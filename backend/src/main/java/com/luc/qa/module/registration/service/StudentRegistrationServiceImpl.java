package com.luc.qa.module.registration.service;

import com.luc.qa.common.email.EmailService;
import com.luc.qa.common.exception.BadRequestException;
import com.luc.qa.common.exception.ConflictException;
import com.luc.qa.common.exception.TooManyRequestsException;
import com.luc.qa.common.keycloak.KeycloakAdminClient;
import com.luc.qa.module.registration.dto.ForgotEmailRequestDTO;
import com.luc.qa.module.registration.dto.ForgotEmailResponseDTO;
import com.luc.qa.module.registration.dto.LookupStudentRequestDTO;
import com.luc.qa.module.registration.dto.LookupStudentResponseDTO;
import com.luc.qa.module.registration.dto.RegisterStudentRequestDTO;
import com.luc.qa.module.registration.dto.RegisterStudentResponseDTO;
import com.luc.qa.module.registration.entity.StudentDirectory;
import com.luc.qa.module.registration.entity.StudentLookupChallenge;
import com.luc.qa.module.registration.repository.StudentDirectoryRepository;
import com.luc.qa.module.registration.repository.StudentLookupChallengeRepository;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StudentRegistrationServiceImpl implements StudentRegistrationService {

    static final String GENERIC_LOOKUP_MESSAGE =
        "If this email is in our records and not already registered, we sent a code.";

    static final String GENERIC_FORGOT_EMAIL_MESSAGE =
        "If this file number is in our records, a hidden version of the email on file is shown below.";

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration RATE_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_LOOKUPS_PER_IP = 8;
    private static final int MAX_LOOKUPS_PER_FILE = 3;
    private static final int MAX_CODE_ATTEMPTS = 5;
    private static final String FORGOT_EMAIL_MARKER = "FORGOT_EMAIL";
    private static final char[] CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final StudentDirectoryRepository studentDirectoryRepository;
    private final StudentLookupChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public LookupStudentResponseDTO startLookup(LookupStudentRequestDTO request, String requesterIp) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        enforceIpRateLimit(requesterIp);

        StudentDirectory directoryEntry = studentDirectoryRepository.findByEmailIgnoreCase(email).orElse(null);
        if (directoryEntry == null) {
            return LookupStudentResponseDTO.builder().message(GENERIC_LOOKUP_MESSAGE).build();
        }
        enforceFileRateLimit(directoryEntry.getFileNumber());
        if (directoryEntry.isRegistered() || userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        sendLookupCode(directoryEntry, requesterIp);
        return LookupStudentResponseDTO.builder().message(GENERIC_LOOKUP_MESSAGE).build();
    }

    @Override
    public RegisterStudentResponseDTO register(RegisterStudentRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirmation do not match");
        }

        StudentDirectory directoryEntry = studentDirectoryRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new BadRequestException("Invalid or expired code"));

        StudentLookupChallenge challenge = challengeRepository
            .findFirstByFileNumberAndLookupTokenHashIsNullAndVerifiedAtIsNullOrderByCreatedAtDesc(
                directoryEntry.getFileNumber()
            )
            .orElseThrow(() -> new BadRequestException("Invalid or expired code"));

        if (challenge.isExpired() || challenge.getAttempts() >= MAX_CODE_ATTEMPTS) {
            throw new BadRequestException("Invalid or expired code");
        }

        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!passwordEncoder.matches(request.getCode().trim().toUpperCase(Locale.ROOT), challenge.getCodeHash())) {
            challengeRepository.save(challenge);
            throw new BadRequestException("Invalid or expired code");
        }

        if (directoryEntry.isRegistered() || userRepository.existsByStudentId(directoryEntry.getFileNumber())) {
            throw new ConflictException("This student file is already registered");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        UUID keycloakId = keycloakAdminClient.createUser(
            email,
            email,
            directoryEntry.getFirstName(),
            directoryEntry.getLastName()
        );
        keycloakAdminClient.setPassword(keycloakId, request.getPassword());
        keycloakAdminClient.assignRealmRole(keycloakId, UserRole.STUDENT.name());

        User user = User.builder()
            .keycloakId(keycloakId)
            .email(email)
            .displayName(directoryEntry.fullName())
            .role(UserRole.STUDENT)
            .studentId(directoryEntry.getFileNumber())
            .verifiedStudentAt(Instant.now())
            .banned(false)
            .build();
        userRepository.save(user);

        directoryEntry.setRegisteredAt(Instant.now());
        studentDirectoryRepository.save(directoryEntry);

        challenge.setVerifiedAt(Instant.now());
        challenge.setConsumedAt(Instant.now());
        challengeRepository.save(challenge);

        return RegisterStudentResponseDTO.builder()
            .message("Your account is ready. Sign in with your email and the password you just chose.")
            .fullName(directoryEntry.fullName())
            .enrollmentYear(directoryEntry.getEnrollmentYear())
            .faculty(directoryEntry.getFaculty())
            .major(directoryEntry.getMajor())
            .build();
    }

    @Override
    public ForgotEmailResponseDTO recoverEmail(ForgotEmailRequestDTO request, String requesterIp) {
        String fileNumber = request.getFileNumber().trim();
        enforceIpRateLimit(requesterIp);
        enforceFileRateLimit(fileNumber);

        StudentLookupChallenge marker = new StudentLookupChallenge();
        marker.setFileNumber(fileNumber);
        marker.setCodeHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        marker.setLookupTokenHash(FORGOT_EMAIL_MARKER);
        marker.setRequesterIp(requesterIp);
        marker.setAttempts(0);
        marker.setCreatedAt(Instant.now());
        marker.setExpiresAt(Instant.now());
        challengeRepository.save(marker);

        StudentDirectory directoryEntry = studentDirectoryRepository.findById(fileNumber).orElse(null);
        if (directoryEntry == null) {
            return ForgotEmailResponseDTO.builder().message(GENERIC_FORGOT_EMAIL_MESSAGE).build();
        }

        return ForgotEmailResponseDTO.builder()
            .message(GENERIC_FORGOT_EMAIL_MESSAGE)
            .maskedEmail(maskEmail(directoryEntry.getEmail()))
            .build();
    }

    private void sendLookupCode(StudentDirectory entry, String requesterIp) {
        String code = randomCode();
        StudentLookupChallenge challenge = new StudentLookupChallenge();
        challenge.setFileNumber(entry.getFileNumber());
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setRequesterIp(requesterIp);
        challenge.setAttempts(0);
        challenge.setCreatedAt(Instant.now());
        challenge.setExpiresAt(Instant.now().plus(CODE_TTL));
        challengeRepository.save(challenge);

        try {
            emailService.sendStudentLookupCode(entry.getEmail(), entry.getFirstName(), code);
        } catch (RuntimeException ex) {
            log.error("Could not send lookup code to {}", entry.getEmail(), ex);
            throw new IllegalStateException("Could not send the lookup code. Check that Mailhog is running.");
        }
        log.info("Sent student lookup code for file {}", entry.getFileNumber());
    }

    private void enforceIpRateLimit(String requesterIp) {
        Instant windowStart = Instant.now().minus(RATE_WINDOW);
        if (challengeRepository.countByRequesterIpAndCreatedAtAfter(requesterIp, windowStart) >= MAX_LOOKUPS_PER_IP) {
            throw new TooManyRequestsException("Too many attempts. Try again in a few minutes.");
        }
    }

    private void enforceFileRateLimit(String fileNumber) {
        Instant windowStart = Instant.now().minus(RATE_WINDOW);
        if (challengeRepository.countByFileNumberAndCreatedAtAfter(fileNumber, windowStart) >= MAX_LOOKUPS_PER_FILE) {
            throw new TooManyRequestsException("Too many attempts. Try again in a few minutes.");
        }
    }

    private String randomCode() {
        char[] buffer = new char[6];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = CODE_ALPHABET[secureRandom.nextInt(CODE_ALPHABET.length)];
        }
        return new String(buffer);
    }

    static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return "*****";
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "*****" + domain;
        }
        int prefix = Math.min(6, local.length() - 1);
        return local.substring(0, prefix) + "*****" + local.charAt(local.length() - 1) + domain;
    }
}
