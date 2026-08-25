package com.luc.qa.module.question.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionSaveResponseDTO {

    private QuestionSummaryDTO question;
    private Instant savedAt;
}
