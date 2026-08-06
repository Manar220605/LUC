package com.luc.qa.module.moderation.service;

import com.luc.qa.module.moderation.dto.CreateReportRequestDTO;
import com.luc.qa.module.moderation.dto.ReportResponseDTO;

public interface ReportService {

    ReportResponseDTO create(CreateReportRequestDTO request, String keycloakId);
}
