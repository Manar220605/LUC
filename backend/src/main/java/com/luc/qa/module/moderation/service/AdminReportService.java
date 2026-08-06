package com.luc.qa.module.moderation.service;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.moderation.dto.AdminReportFilterDTO;
import com.luc.qa.module.moderation.dto.AdminReportResponseDTO;
import com.luc.qa.module.moderation.dto.ResolveReportRequestDTO;

public interface AdminReportService {

    PageResponseDTO<AdminReportResponseDTO> listReports(AdminReportFilterDTO filter);

    AdminReportResponseDTO resolve(Long id, String resolverKeycloakId, ResolveReportRequestDTO request);
}
