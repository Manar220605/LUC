package com.luc.qa.module.registration.service;

import com.luc.qa.module.registration.dto.RegisterStudentRequestDTO;
import com.luc.qa.module.registration.dto.RegisterStudentResponseDTO;

public interface StudentRegistrationService {

    RegisterStudentResponseDTO register(RegisterStudentRequestDTO request);
}
