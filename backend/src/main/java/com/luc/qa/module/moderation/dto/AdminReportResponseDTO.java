package com.luc.qa.module.moderation.dto;

import com.luc.qa.module.moderation.entity.ReportReason;
import com.luc.qa.module.moderation.entity.ReportStatus;
import com.luc.qa.module.moderation.entity.ReportTargetType;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminReportResponseDTO {

    Long id;
    Long reporterId;
    String reporterDisplayName;
    String reporterEmail;
    ReportTargetType targetType;
    Long targetId;
    ReportReason reason;
    String details;
    ReportStatus status;
    Instant createdAt;
    Instant resolvedAt;
    String resolutionNote;
    String resolverDisplayName;
    String targetPreview;
    Long targetQuestionId;
    ModeratorAuthorDTO targetAuthor;
}
