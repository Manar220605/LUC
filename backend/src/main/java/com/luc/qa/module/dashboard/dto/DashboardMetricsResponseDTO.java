package com.luc.qa.module.dashboard.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DashboardMetricsResponseDTO {

    long totalUsers;
    long totalAlumni;
    long totalQuestions;
    long totalAnswers;
    long pendingVerifications;
    long openReports;
}
