package com.luc.qa.module.registration.controller;

import com.luc.qa.module.registration.dto.ForgotEmailRequestDTO;
import com.luc.qa.module.registration.dto.ForgotEmailResponseDTO;
import com.luc.qa.module.registration.dto.LookupStudentRequestDTO;
import com.luc.qa.module.registration.dto.LookupStudentResponseDTO;
import com.luc.qa.module.registration.dto.RegisterStudentRequestDTO;
import com.luc.qa.module.registration.dto.RegisterStudentResponseDTO;
import com.luc.qa.module.registration.service.StudentRegistrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
@Tag(name = "Registration")
public class StudentRegistrationController {

    private final StudentRegistrationService studentRegistrationService;

    @PostMapping("/student/lookup")
    public LookupStudentResponseDTO lookupStudent(
        @Valid @RequestBody LookupStudentRequestDTO request,
        HttpServletRequest httpRequest
    ) {
        return studentRegistrationService.startLookup(request, clientIp(httpRequest));
    }

    @PostMapping("/student")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterStudentResponseDTO registerStudent(@Valid @RequestBody RegisterStudentRequestDTO request) {
        return studentRegistrationService.register(request);
    }

    @PostMapping("/forgot-email")
    public ForgotEmailResponseDTO forgotEmail(
        @Valid @RequestBody ForgotEmailRequestDTO request,
        HttpServletRequest httpRequest
    ) {
        return studentRegistrationService.recoverEmail(request, clientIp(httpRequest));
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String remote = request.getRemoteAddr();
        return remote != null ? remote : "unknown";
    }
}
