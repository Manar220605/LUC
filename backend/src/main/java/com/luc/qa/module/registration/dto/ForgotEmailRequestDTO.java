package com.luc.qa.module.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotEmailRequestDTO {

    @NotBlank
    @Size(max = 20)
    private String fileNumber;
}
