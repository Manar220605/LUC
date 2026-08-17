package com.luc.qa.module.registration.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterStudentResponseDTO {

    private String message;
    private String fullName;
    private int enrollmentYear;
    private String faculty;
    private String major;
}
