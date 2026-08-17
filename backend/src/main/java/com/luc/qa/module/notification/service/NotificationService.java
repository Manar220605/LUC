package com.luc.qa.module.notification.service;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.answer.entity.Answer;
import com.luc.qa.module.notification.dto.NotificationResponseDTO;
import com.luc.qa.module.notification.dto.UnreadCountResponseDTO;
import com.luc.qa.module.question.entity.Question;
import com.luc.qa.module.user.entity.User;
import com.luc.qa.module.vote.entity.VoteTargetType;

public interface NotificationService {

    void notifyAnswerOnQuestion(User actor, Question question, Answer answer);

    void notifyUpvote(
        User actor,
        User recipient,
        Question question,
        VoteTargetType targetType,
        Long targetId
    );

    void notifyMentions(
        User actor,
        String body,
        Question question,
        VoteTargetType targetType,
        Long targetId,
        boolean actorAnonymous
    );

    PageResponseDTO<NotificationResponseDTO> listForCurrentUser(String keycloakId, int page, int size);

    UnreadCountResponseDTO getUnreadCount(String keycloakId);

    NotificationResponseDTO markAsRead(Long notificationId, String keycloakId);

    void markAllAsRead(String keycloakId);
}
