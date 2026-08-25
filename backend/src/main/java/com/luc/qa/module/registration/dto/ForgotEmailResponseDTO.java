package com.luc.qa.module.registration.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForgotEmailResponseDTO {

    private String message;
    private String maskedEmail;
}
