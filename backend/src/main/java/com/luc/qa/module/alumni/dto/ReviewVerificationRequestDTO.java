package com.luc.qa.module.alumni.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewVerificationRequestDTO {

    @NotBlank
    @Size(max = 2000)
    private String rejectionReason;
}
