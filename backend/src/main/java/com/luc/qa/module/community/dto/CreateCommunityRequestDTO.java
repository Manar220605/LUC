package com.luc.qa.module.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCommunityRequestDTO {

    @NotBlank
    @Pattern(regexp = "^[a-z0-9_]{1,50}$")
    private String slug;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 5000)
    private String description;

    private String parentPath;
}
