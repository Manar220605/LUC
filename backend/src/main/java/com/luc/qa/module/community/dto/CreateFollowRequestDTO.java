package com.luc.qa.module.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFollowRequestDTO {

    @NotBlank
    private String communityPath;
}
