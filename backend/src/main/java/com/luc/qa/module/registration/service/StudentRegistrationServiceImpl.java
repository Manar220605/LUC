package com.luc.qa.module.registration.service;

import com.luc.qa.common.email.EmailService;
import com.luc.qa.common.exception.BadRequestException;
import com.luc.qa.common.exception.ConflictException;
import com.luc.qa.common.keycloak.KeycloakAdminClient;
import com.luc.qa.common.util.SecurePasswordGenerator;
import com.luc.qa.module.registration.dto.RegisterStudentRequestDTO;
import com.luc.qa.module.registration.dto.RegisterStudentResponseDTO;
import com.luc.qa.module.registration.entity.StudentDirectory;
import com.luc.qa.module.registration.repository.StudentDirectoryRepository;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentRegistrationServiceImpl implements StudentRegistrationService {

    private static final int GENERATED_PASSWORD_LENGTH = 14;

    private final StudentDirectoryRepository studentDirectoryRepository;
    private final UserRepository userRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final EmailService emailService;

    @Override
    public RegisterStudentResponseDTO register(RegisterStudentRequestDTO request) {
        String fileNumber = request.getFileNumber().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        StudentDirectory directoryEntry = studentDirectoryRepository
            .findByFileNumberAndEmailIgnoreCase(fileNumber, email)
            .orElseThrow(() -> new BadRequestException(
                "File number and email do not match our university records"
            ));

        if (directoryEntry.isRegistered()) {
            throw new ConflictException("This student file is already registered");
        }
        if (userRepository.existsByStudentId(fileNumber)) {
            throw new ConflictException("This student file is already registered");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        String password = SecurePasswordGenerator.generate(GENERATED_PASSWORD_LENGTH);
        UUID keycloakId = keycloakAdminClient.createUser(
            email,
            email,
            directoryEntry.getFirstName(),
            directoryEntry.getLastName()
        );
        keycloakAdminClient.setPassword(keycloakId, password);
        keycloakAdminClient.assignRealmRole(keycloakId, UserRole.STUDENT.name());

        User user = User.builder()
            .keycloakId(keycloakId)
            .email(email)
            .displayName(directoryEntry.fullName())
            .role(UserRole.STUDENT)
            .studentId(fileNumber)
            .verifiedStudentAt(Instant.now())
            .banned(false)
            .build();
        userRepository.save(user);

        directoryEntry.setRegisteredAt(Instant.now());
        studentDirectoryRepository.save(directoryEntry);

        emailService.sendStudentCredentials(email, directoryEntry.fullName(), password);

        return RegisterStudentResponseDTO.builder()
            .message("Your account has been created. Check your university email for login credentials.")
            .fullName(directoryEntry.fullName())
            .enrollmentYear(directoryEntry.getEnrollmentYear())
            .faculty(directoryEntry.getFaculty())
            .major(directoryEntry.getMajor())
            .build();
    }
}
