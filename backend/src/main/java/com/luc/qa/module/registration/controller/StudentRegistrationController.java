package com.luc.qa.module.registration.controller;

import com.luc.qa.module.registration.dto.RegisterStudentRequestDTO;
import com.luc.qa.module.registration.dto.RegisterStudentResponseDTO;
import com.luc.qa.module.registration.service.StudentRegistrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostMapping("/student")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterStudentResponseDTO registerStudent(@Valid @RequestBody RegisterStudentRequestDTO request) {
        return studentRegistrationService.register(request);
    }
}
