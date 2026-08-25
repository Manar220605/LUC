package com.luc.qa.module.question.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSaveRequestDTO {

    @NotNull
    private Long questionId;
}
