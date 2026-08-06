package com.luc.qa.module.moderation.dto;

import com.luc.qa.module.moderation.entity.ReportReason;
import com.luc.qa.module.moderation.entity.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReportRequestDTO {

    @NotNull
    private ReportTargetType targetType;

    @NotNull
    private Long targetId;

    @NotNull
    private ReportReason reason;

    @Size(max = 2000)
    private String details;
}
