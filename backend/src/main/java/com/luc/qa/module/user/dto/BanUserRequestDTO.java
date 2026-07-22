package com.luc.qa.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BanUserRequestDTO {

    @NotBlank
    private String reason;
}
