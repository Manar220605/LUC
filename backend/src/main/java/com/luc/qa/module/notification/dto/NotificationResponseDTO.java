package com.luc.qa.module.notification.dto;

import com.luc.qa.module.notification.entity.NotificationType;
import com.luc.qa.module.user.dto.PublicAuthorDTO;
import com.luc.qa.module.vote.entity.VoteTargetType;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponseDTO {

    private Long id;
    private NotificationType type;
    private String message;
    private PublicAuthorDTO actor;
    private VoteTargetType targetType;
    private Long targetId;
    private Long questionId;
    private boolean read;
    private Instant createdAt;
}
