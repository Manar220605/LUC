package com.luc.qa.module.notification.mapper;

import com.luc.qa.module.notification.dto.NotificationResponseDTO;
import com.luc.qa.module.notification.entity.Notification;
import com.luc.qa.module.notification.entity.NotificationType;
import com.luc.qa.module.user.dto.PublicAuthorDTO;
import com.luc.qa.module.user.service.PublicAuthorService;
import com.luc.qa.module.vote.entity.VoteTargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    @Autowired
    private PublicAuthorService publicAuthorService;

    public NotificationResponseDTO toResponse(Notification notification) {
        PublicAuthorDTO actor = mapActor(notification);
        return NotificationResponseDTO.builder()
            .id(notification.getId())
            .type(notification.getType())
            .message(buildMessage(notification, actor))
            .actor(actor)
            .targetType(notification.getTargetType())
            .targetId(notification.getTargetId())
            .questionId(notification.getQuestion() != null ? notification.getQuestion().getId() : null)
            .read(notification.getReadAt() != null)
            .createdAt(notification.getCreatedAt())
            .build();
    }

    private PublicAuthorDTO mapActor(Notification notification) {
        if (notification.getActor() == null) {
            return publicAuthorService.anonymous();
        }
        return publicAuthorService.fromUser(notification.getActor());
    }

    private String buildMessage(Notification notification, PublicAuthorDTO actor) {
        String actorName = actor.getDisplayName();
        return switch (notification.getType()) {
            case ANSWER_ON_QUESTION -> actorName + " answered your question";
            case UPVOTE -> notification.getTargetType() == VoteTargetType.QUESTION
                ? actorName + " upvoted your question"
                : actorName + " upvoted your answer";
            case MENTION -> actorName + " mentioned you";
            case MENTORSHIP_REQUEST -> actorName + " asked you to be a mentor";
            case MENTORSHIP_ACCEPTED -> actorName + " accepted your mentorship request";
            case MENTORSHIP_DECLINED -> actorName + " declined your mentorship request";
            case NEW_QUESTION_IN_COMMUNITY -> {
                String communityName = notification.getQuestion() != null
                    && notification.getQuestion().getCommunity() != null
                    ? notification.getQuestion().getCommunity().getName()
                    : "a community you follow";
                yield actorName + " posted a question in " + communityName;
            }
        };
    }
}
