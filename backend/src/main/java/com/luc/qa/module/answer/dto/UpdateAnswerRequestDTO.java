package com.luc.qa.module.answer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAnswerRequestDTO {

    @NotBlank
    private String body;

    private boolean anonymous;
}
