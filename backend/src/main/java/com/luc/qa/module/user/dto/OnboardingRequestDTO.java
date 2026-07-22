package com.luc.qa.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OnboardingRequestDTO {

    @NotBlank
    @Size(max = 20)
    private String studentId;
}
