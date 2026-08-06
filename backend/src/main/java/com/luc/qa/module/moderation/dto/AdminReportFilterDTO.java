package com.luc.qa.module.moderation.dto;

import com.luc.qa.module.moderation.entity.ReportStatus;
import com.luc.qa.module.moderation.entity.ReportTargetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportFilterDTO {

    private ReportStatus status;
    private ReportTargetType targetType;
    private int page = 0;
    private int size = 20;
}
