package com.luc.qa.module.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateQuestionRequestDTO {

    @NotBlank
    private String communityPath;

    @NotBlank
    @Size(max = 300)
    private String title;

    @NotBlank
    private String body;

    private boolean anonymous;
}
