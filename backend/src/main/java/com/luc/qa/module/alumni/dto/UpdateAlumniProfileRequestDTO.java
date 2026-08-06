package com.luc.qa.module.alumni.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAlumniProfileRequestDTO {

    @Size(max = 200)
    private String currentPosition;

    @Size(max = 200)
    private String currentCompany;

    private Boolean isPublic;
}
