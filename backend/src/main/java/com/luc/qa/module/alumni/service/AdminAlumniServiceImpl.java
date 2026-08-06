package com.luc.qa.module.alumni.service;

import com.luc.qa.common.exception.BadRequestException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.common.exception.VerificationNotFoundException;
import com.luc.qa.common.keycloak.KeycloakAdminClient;
import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.alumni.dto.AdminVerificationFilterDTO;
import com.luc.qa.module.alumni.dto.AdminVerificationResponseDTO;
import com.luc.qa.module.alumni.dto.ReviewVerificationRequestDTO;
import com.luc.qa.module.alumni.dto.VerificationResponseDTO;
import com.luc.qa.module.alumni.entity.AlumniProfile;
import com.luc.qa.module.alumni.entity.AlumniVerification;
import com.luc.qa.module.alumni.entity.VerificationStatus;
import com.luc.qa.module.alumni.mapper.AlumniVerificationMapper;
import com.luc.qa.module.alumni.repository.AlumniProfileRepository;
import com.luc.qa.module.alumni.repository.AlumniVerificationRepository;
import com.luc.qa.module.alumni.specification.VerificationSpecifications;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminAlumniServiceImpl implements AdminAlumniService {

    private final AlumniVerificationRepository verificationRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final UserRepository userRepository;
    private final AlumniVerificationMapper verificationMapper;
    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminVerificationResponseDTO> listVerifications(AdminVerificationFilterDTO filter) {
        Specification<AlumniVerification> spec = VerificationSpecifications.fromFilter(filter);
        PageRequest pageable = PageRequest.of(
            filter.getPage(),
            filter.getSize(),
            Sort.by(Sort.Direction.DESC, "submittedAt")
        );
        Page<AlumniVerification> page = verificationRepository.findAll(spec, pageable);
        List<AdminVerificationResponseDTO> content = page.getContent().stream()
            .map(verificationMapper::toAdminResponse)
            .toList();
        return PageResponseDTO.<AdminVerificationResponseDTO>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }

    @Override
    public VerificationResponseDTO approve(Long id, String reviewerKeycloakId) {
        AlumniVerification verification = loadPendingVerification(id);
        User reviewer = findUser(reviewerKeycloakId);
        User user = userRepository.findById(verification.getUser().getId())
            .orElseThrow(() -> new UserNotFoundException(verification.getUser().getId()));
        Instant now = Instant.now();

        verification.setStatus(VerificationStatus.APPROVED);
        verification.setReviewer(reviewer);
        verification.setReviewedAt(now);
        verification.setRejectionReason(null);

        AlumniProfile profile = alumniProfileRepository.findById(user.getId())
            .orElseGet(() -> AlumniProfile.builder()
                .userId(user.getId())
                .publicProfile(true)
                .build());
        profile.setGradYear(verification.getClaimedGradYear());
        profile.setFaculty(verification.getClaimedFaculty());
        profile.setDegree(verification.getClaimedDegree());
        profile.setMajor(verification.getClaimedMajor());
        profile.setCurrentPosition(verification.getClaimedPosition());
        profile.setCurrentCompany(verification.getClaimedCompany());
        profile.setLinkedinUrl(verification.getLinkedinUrl());
        alumniProfileRepository.save(profile);

        user.setRole(UserRole.ALUMNI);
        user.setVerifiedAlumniAt(now);
        userRepository.save(user);

        try {
            keycloakAdminClient.assignRealmRole(user.getKeycloakId(), "ALUMNI");
        } catch (Exception ex) {
            log.error("Failed to assign ALUMNI role in Keycloak for user {}", user.getKeycloakId(), ex);
            throw new BadRequestException("Failed to assign ALUMNI role in Keycloak: " + ex.getMessage());
        }

        return verificationMapper.toResponse(verificationRepository.save(verification));
    }

    @Override
    public VerificationResponseDTO reject(
        Long id,
        String reviewerKeycloakId,
        ReviewVerificationRequestDTO request
    ) {
        AlumniVerification verification = loadPendingVerification(id);
        User reviewer = findUser(reviewerKeycloakId);

        verification.setStatus(VerificationStatus.REJECTED);
        verification.setReviewer(reviewer);
        verification.setReviewedAt(Instant.now());
        verification.setRejectionReason(request.getRejectionReason().trim());

        return verificationMapper.toResponse(verificationRepository.save(verification));
    }

    private AlumniVerification loadPendingVerification(Long id) {
        AlumniVerification verification = verificationRepository.findById(id)
            .orElseThrow(() -> new VerificationNotFoundException(id));
        if (verification.getStatus() != VerificationStatus.PENDING) {
            throw new BadRequestException("Only pending verification requests can be reviewed");
        }
        return verification;
    }

    private User findUser(String keycloakId) {
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
    }
}
