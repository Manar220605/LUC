package com.luc.qa.module.notification.service;

import com.luc.qa.common.exception.ForbiddenException;
import com.luc.qa.common.exception.NotificationNotFoundException;
import com.luc.qa.common.exception.UserNotFoundException;
import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.notification.dto.NotificationResponseDTO;
import com.luc.qa.module.notification.dto.UnreadCountResponseDTO;
import com.luc.qa.module.notification.entity.Notification;
import com.luc.qa.module.notification.entity.NotificationType;
import com.luc.qa.module.notification.mapper.NotificationMapper;
import com.luc.qa.module.notification.repository.NotificationRepository;
import com.luc.qa.module.notification.util.MentionParser;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.user.repository.UserRepository;
import com.luc.qa.module.vote.entity.VoteTargetType;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public void notifyAnswerOnQuestion(User actor, Question question, Answer answer) {
        User recipient = question.getAuthor();
        if (shouldSkip(actor, recipient)) {
            return;
        }

        saveNotification(
            recipient,
            actor,
            answer.isAnonymous(),
            NotificationType.ANSWER_ON_QUESTION,
            VoteTargetType.ANSWER,
            answer.getId(),
            question
        );
    }

    @Override
    public void notifyUpvote(
        User actor,
        User recipient,
        Question question,
        VoteTargetType targetType,
        Long targetId
    ) {
        if (shouldSkip(actor, recipient)) {
            return;
        }

        saveNotification(
            recipient,
            actor,
            false,
            NotificationType.UPVOTE,
            targetType,
            targetId,
            question
        );
    }

    @Override
    public void notifyMentions(
        User actor,
        String body,
        Question question,
        VoteTargetType targetType,
        Long targetId,
        boolean actorAnonymous
    ) {
        Set<String> handles = MentionParser.parseHandles(body);
        if (handles.isEmpty()) {
            return;
        }

        for (String handle : handles) {
            userRepository.findByDisplayNameIgnoreCase(handle).ifPresent(recipient -> {
                if (shouldSkip(actor, recipient)) {
                    return;
                }
                if (notificationRepository.existsByRecipientIdAndTypeAndTargetTypeAndTargetId(
                    recipient.getId(),
                    NotificationType.MENTION,
                    targetType,
                    targetId
                )) {
                    return;
                }
                saveNotification(
                    recipient,
                    actor,
                    actorAnonymous,
                    NotificationType.MENTION,
                    targetType,
                    targetId,
                    question
                );
            });
        }
    }

    @Override
    public void notifyMentorshipRequest(User student, User alumni, Long requestId) {
        if (shouldSkip(student, alumni)) {
            return;
        }
        saveNotification(
            alumni,
            student,
            false,
            NotificationType.MENTORSHIP_REQUEST,
            null,
            requestId,
            null
        );
    }

    @Override
    public void notifyMentorshipDecision(User alumni, User student, Long requestId, boolean accepted) {
        if (shouldSkip(alumni, student)) {
            return;
        }
        saveNotification(
            student,
            alumni,
            false,
            accepted ? NotificationType.MENTORSHIP_ACCEPTED : NotificationType.MENTORSHIP_DECLINED,
            null,
            requestId,
            null
        );
    }

    @Override
    public void notifyNewQuestionInCommunity(
        User author,
        User follower,
        Question question,
        boolean authorAnonymous
    ) {
        if (shouldSkip(author, follower)) {
            return;
        }
        saveNotification(
            follower,
            author,
            authorAnonymous,
            NotificationType.NEW_QUESTION_IN_COMMUNITY,
            VoteTargetType.QUESTION,
            question.getId(),
            question
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<NotificationResponseDTO> listForCurrentUser(
        String keycloakId,
        int page,
        int size,
        boolean unreadOnly
    ) {
        User recipient = resolveUser(keycloakId);
        PageRequest pageable = PageRequest.of(page, size);
        Page<Notification> results = unreadOnly
            ? notificationRepository.findByRecipientIdAndReadAtIsNullOrderByCreatedAtDesc(
                recipient.getId(),
                pageable
            )
            : notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId(), pageable);

        return PageResponseDTO.<NotificationResponseDTO>builder()
            .content(results.getContent().stream().map(notificationMapper::toResponse).toList())
            .page(results.getNumber())
            .size(results.getSize())
            .totalElements(results.getTotalElements())
            .totalPages(results.getTotalPages())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponseDTO getUnreadCount(String keycloakId) {
        User recipient = resolveUser(keycloakId);
        long count = notificationRepository.countByRecipientIdAndReadAtIsNull(recipient.getId());
        return UnreadCountResponseDTO.builder().count(count).build();
    }

    @Override
    public NotificationResponseDTO markAsRead(Long notificationId, String keycloakId) {
        User recipient = resolveUser(keycloakId);
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getRecipient().getId().equals(recipient.getId())) {
            throw new ForbiddenException("You can only read your own notifications");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    public void markAllAsRead(String keycloakId) {
        User recipient = resolveUser(keycloakId);
        notificationRepository.markAllReadForRecipient(recipient.getId());
    }

    private void saveNotification(
        User recipient,
        User actor,
        boolean hideActor,
        NotificationType type,
        VoteTargetType targetType,
        Long targetId,
        Question question
    ) {
        notificationRepository.save(Notification.builder()
            .recipient(recipient)
            .actor(hideActor ? null : actor)
            .type(type)
            .targetType(targetType)
            .targetId(targetId)
            .question(question)
            .build());
    }

    private boolean shouldSkip(User actor, User recipient) {
        return actor != null && actor.getId().equals(recipient.getId());
    }

    private User resolveUser(String keycloakId) {
        return userRepository.findByKeycloakId(UUID.fromString(keycloakId))
            .orElseThrow(() -> new UserNotFoundException("Keycloak sub " + keycloakId));
    }
}
