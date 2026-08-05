package com.luc.qa.module.answer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAnswerRequestDTO {

    @NotBlank
    private String body;

    private boolean anonymous;
}
