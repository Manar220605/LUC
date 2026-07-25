package com.luc.qa.module.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCommunityRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 5000)
    private String description;
}
