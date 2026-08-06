package com.luc.qa.module.alumni.service;

import com.luc.qa.module.alumni.dto.SubmitVerificationRequestDTO;
import com.luc.qa.module.alumni.dto.VerificationResponseDTO;
import com.luc.qa.module.alumni.entity.AlumniVerification;

public interface AlumniVerificationService {

    VerificationResponseDTO submit(String keycloakId, SubmitVerificationRequestDTO request);

    VerificationResponseDTO getMine(String keycloakId);
}
