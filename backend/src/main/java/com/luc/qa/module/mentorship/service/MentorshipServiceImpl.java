package com.luc.qa.module.mentorship.service;

import com.luc.qa.common.exception.BadRequestException;
import com.luc.qa.common.exception.ForbiddenException;
import com.luc.qa.common.exception.MentorshipAlreadyAcceptedException;
import com.luc.qa.common.exception.MentorshipRequestAlreadyOpenException;
import com.luc.qa.common.exception.MentorshipRequestNotFoundException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.module.alumni.entity.AlumniProfile;
import com.luc.qa.module.alumni.repository.AlumniProfileRepository;
import com.luc.qa.module.mentorship.dto.CreateMentorshipRequestDTO;
import com.luc.qa.module.mentorship.dto.MentorshipInboxDTO;
import com.luc.qa.module.mentorship.dto.MentorshipRequestResponseDTO;
import com.luc.qa.module.mentorship.dto.MentorshipStatusDTO;
import com.luc.qa.module.mentorship.entity.MentorshipRequest;
import com.luc.qa.module.mentorship.entity.MentorshipRequestStatus;
import com.luc.qa.module.mentorship.mapper.MentorshipRequestMapper;
import com.luc.qa.module.mentorship.repository.MentorshipRequestRepository;
import com.luc.qa.module.notification.service.NotificationService;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.entity.UserRole;
import com.luc.qa.module.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class MentorshipServiceImpl implements MentorshipService {

    private final MentorshipRequestRepository mentorshipRequestRepository;
    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final MentorshipRequestMapper mentorshipRequestMapper;
    private final NotificationService notificationService;

    @Override
    public MentorshipRequestResponseDTO create(String keycloakId, CreateMentorshipRequestDTO request) {
        User student = findUser(keycloakId);
        if (student.getRole() != UserRole.STUDENT) {
            throw new ForbiddenException("Only students can ask for mentorship");
        }

        User alumni = userRepository.findById(request.getAlumniUserId())
            .orElseThrow(() -> new UserNotFoundException(request.getAlumniUserId()));
        requirePublicAlumni(student, alumni);

        if (mentorshipRequestRepository.existsByStudentIdAndAlumniIdAndStatus(
            student.getId(),
            alumni.getId(),
            MentorshipRequestStatus.PENDING
        )) {
            throw new MentorshipRequestAlreadyOpenException();
        }
        if (mentorshipRequestRepository.existsByStudentIdAndAlumniIdAndStatus(
            student.getId(),
            alumni.getId(),
            MentorshipRequestStatus.ACCEPTED
        )) {
            throw new MentorshipAlreadyAcceptedException();
        }

        MentorshipRequest saved = mentorshipRequestRepository.save(MentorshipRequest.builder()
            .student(student)
            .alumni(alumni)
            .message(request.getMessage().trim())
            .status(MentorshipRequestStatus.PENDING)
            .build());

        notificationService.notifyMentorshipRequest(student, alumni, saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MentorshipInboxDTO listMine(String keycloakId) {
        User user = findUser(keycloakId);
        return MentorshipInboxDTO.builder()
            .incoming(mentorshipRequestRepository.findByAlumniIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList())
            .outgoing(mentorshipRequestRepository.findByStudentIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MentorshipStatusDTO statusWith(String keycloakId, Long alumniUserId) {
        User current = findUser(keycloakId);
        if (current.getId().equals(alumniUserId)) {
            return MentorshipStatusDTO.builder()
                .canRequest(false)
                .cannotRequestReason("SELF")
                .build();
        }
        if (current.getRole() != UserRole.STUDENT) {
            return MentorshipStatusDTO.builder()
                .canRequest(false)
                .cannotRequestReason("STUDENT_ONLY")
                .build();
        }

        User alumni = userRepository.findById(alumniUserId)
            .orElseThrow(() -> new UserNotFoundException(alumniUserId));
        if (!isPublicAlumni(alumni)) {
            return MentorshipStatusDTO.builder()
                .canRequest(false)
                .cannotRequestReason("NOT_ALUMNI")
                .build();
        }

        MentorshipRequest existing = mentorshipRequestRepository
            .findTopByStudentIdAndAlumniIdOrderByCreatedAtDesc(current.getId(), alumni.getId())
            .orElse(null);
        if (existing == null) {
            return MentorshipStatusDTO.builder().canRequest(true).build();
        }
        if (existing.getStatus() == MentorshipRequestStatus.PENDING) {
            return MentorshipStatusDTO.builder()
                .canRequest(false)
                .cannotRequestReason("PENDING")
                .existing(toResponse(existing))
                .build();
        }
        if (existing.getStatus() == MentorshipRequestStatus.ACCEPTED) {
            return MentorshipStatusDTO.builder()
                .canRequest(false)
                .cannotRequestReason("ACCEPTED")
                .existing(toResponse(existing))
                .build();
        }
        return MentorshipStatusDTO.builder()
            .canRequest(true)
            .cannotRequestReason("DECLINED")
            .existing(toResponse(existing))
            .build();
    }

    @Override
    public MentorshipRequestResponseDTO accept(String keycloakId, Long requestId) {
        return respond(keycloakId, requestId, MentorshipRequestStatus.ACCEPTED);
    }

    @Override
    public MentorshipRequestResponseDTO decline(String keycloakId, Long requestId) {
        return respond(keycloakId, requestId, MentorshipRequestStatus.DECLINED);
    }

    private MentorshipRequestResponseDTO respond(
        String keycloakId,
        Long requestId,
        MentorshipRequestStatus nextStatus
    ) {
        User alumni = findUser(keycloakId);
        MentorshipRequest request = mentorshipRequestRepository.findByIdAndAlumniId(requestId, alumni.getId())
            .orElseThrow(() -> new MentorshipRequestNotFoundException(requestId));
        if (request.getStatus() != MentorshipRequestStatus.PENDING) {
            throw new BadRequestException("This request is no longer pending");
        }

        request.setStatus(nextStatus);
        request.setRespondedAt(Instant.now());
        MentorshipRequest saved = mentorshipRequestRepository.save(request);
        notificationService.notifyMentorshipDecision(
            alumni,
            request.getStudent(),
            saved.getId(),
            nextStatus == MentorshipRequestStatus.ACCEPTED
        );
        return toResponse(saved);
    }

    private void requirePublicAlumni(User student, User alumni) {
        if (student.getId().equals(alumni.getId())) {
            throw new BadRequestException("You cannot ask yourself for mentorship");
        }
        if (!isPublicAlumni(alumni)) {
            throw new BadRequestException("This person is not available for mentorship");
        }
    }

    private boolean isPublicAlumni(User alumni) {
        if (alumni.getRole() != UserRole.ALUMNI || alumni.isBanned()) {
            return false;
        }
        return alumniProfileRepository.findById(alumni.getId())
            .filter(AlumniProfile::isPublicProfile)
            .isPresent();
    }

    private MentorshipRequestResponseDTO toResponse(MentorshipRequest request) {
        String linkedin = alumniProfileRepository.findById(request.getAlumni().getId())
            .map(AlumniProfile::getLinkedinUrl)
            .filter(StringUtils::hasText)
            .map(String::trim)
            .orElse(null);
        return mentorshipRequestMapper.toResponse(request).toBuilder()
            .alumniLinkedinUrl(linkedin)
            .build();
    }

    private User findUser(String keycloakId) {
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
    }
}
