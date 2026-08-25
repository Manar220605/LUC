package com.luc.qa.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String displayName;

    @Size(max = 5000)
    private String bio;
}
