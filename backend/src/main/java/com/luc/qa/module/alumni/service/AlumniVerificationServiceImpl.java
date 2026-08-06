package com.luc.qa.module.alumni.service;

import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.common.exception.VerificationAlreadyPendingException;
import com.luc.qa.common.exception.VerificationNotFoundException;
import com.luc.qa.module.alumni.dto.SubmitVerificationRequestDTO;
import com.luc.qa.module.alumni.dto.VerificationResponseDTO;
import com.luc.qa.module.alumni.entity.AlumniVerification;
import com.luc.qa.module.alumni.entity.VerificationStatus;
import com.luc.qa.module.alumni.mapper.AlumniVerificationMapper;
import com.luc.qa.module.alumni.repository.AlumniVerificationRepository;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlumniVerificationServiceImpl implements AlumniVerificationService {

    private final AlumniVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final AlumniVerificationMapper verificationMapper;

    @Override
    public VerificationResponseDTO submit(String keycloakId, SubmitVerificationRequestDTO request) {
        User user = findUser(keycloakId);
        if (verificationRepository.existsByUserIdAndStatus(user.getId(), VerificationStatus.PENDING)) {
            throw new VerificationAlreadyPendingException();
        }

        AlumniVerification verification = AlumniVerification.builder()
            .user(user)
            .linkedinUrl(request.getLinkedinUrl().trim())
            .claimedGradYear(request.getClaimedGradYear())
            .claimedFaculty(request.getClaimedFaculty())
            .claimedDegree(request.getClaimedDegree())
            .claimedMajor(request.getClaimedMajor().trim())
            .claimedPosition(trimToNull(request.getClaimedPosition()))
            .claimedCompany(trimToNull(request.getClaimedCompany()))
            .status(VerificationStatus.PENDING)
            .submittedAt(Instant.now())
            .build();

        return verificationMapper.toResponse(verificationRepository.save(verification));
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationResponseDTO getMine(String keycloakId) {
        User user = findUser(keycloakId);
        AlumniVerification verification = verificationRepository
            .findTopByUserIdOrderBySubmittedAtDesc(user.getId())
            .orElseThrow(() -> new VerificationNotFoundException("No verification request found for current user"));
        return verificationMapper.toResponse(verification);
    }

    private User findUser(String keycloakId) {
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
