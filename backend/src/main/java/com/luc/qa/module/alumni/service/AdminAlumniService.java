package com.luc.qa.module.alumni.service;

import com.luc.qa.common.pagination.PageResponseDTO;
import com.luc.qa.module.alumni.dto.AdminVerificationFilterDTO;
import com.luc.qa.module.alumni.dto.AdminVerificationResponseDTO;
import com.luc.qa.module.alumni.dto.ReviewVerificationRequestDTO;
import com.luc.qa.module.alumni.dto.VerificationResponseDTO;

public interface AdminAlumniService {

    PageResponseDTO<AdminVerificationResponseDTO> listVerifications(AdminVerificationFilterDTO filter);

    VerificationResponseDTO approve(Long id, String reviewerKeycloakId);

    VerificationResponseDTO reject(Long id, String reviewerKeycloakId, ReviewVerificationRequestDTO request);
}
