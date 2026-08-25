package com.luc.qa.module.registration.service;

import com.luc.qa.module.registration.dto.ForgotEmailRequestDTO;
import com.luc.qa.module.registration.dto.ForgotEmailResponseDTO;
import com.luc.qa.module.registration.dto.LookupStudentRequestDTO;
import com.luc.qa.module.registration.dto.LookupStudentResponseDTO;
import com.luc.qa.module.registration.dto.RegisterStudentRequestDTO;
import com.luc.qa.module.registration.dto.RegisterStudentResponseDTO;

public interface StudentRegistrationService {

    LookupStudentResponseDTO startLookup(LookupStudentRequestDTO request, String requesterIp);

    RegisterStudentResponseDTO register(RegisterStudentRequestDTO request);

    ForgotEmailResponseDTO recoverEmail(ForgotEmailRequestDTO request, String requesterIp);
}
