package com.luc.qa.module.alumni.dto;

import com.luc.qa.module.alumni.entity.Degree;
import com.luc.qa.module.alumni.entity.Faculty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitVerificationRequestDTO {

    @NotBlank
    @Size(max = 500)
    private String linkedinUrl;

    @NotNull
    @Min(1950)
    @Max(2100)
    private Integer claimedGradYear;

    @NotNull
    private Faculty claimedFaculty;

    @NotNull
    private Degree claimedDegree;

    @NotBlank
    @Size(max = 100)
    private String claimedMajor;

    @Size(max = 200)
    private String claimedPosition;

    @Size(max = 200)
    private String claimedCompany;
}
