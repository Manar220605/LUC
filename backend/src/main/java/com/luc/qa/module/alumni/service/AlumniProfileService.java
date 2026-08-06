package com.luc.qa.module.alumni.service;

import com.luc.qa.module.alumni.dto.AlumniProfileResponseDTO;
import com.luc.qa.module.alumni.dto.UpdateAlumniProfileRequestDTO;

public interface AlumniProfileService {

    AlumniProfileResponseDTO getMine(String keycloakId);

    AlumniProfileResponseDTO updateMine(String keycloakId, UpdateAlumniProfileRequestDTO request);
}
