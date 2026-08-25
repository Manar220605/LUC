package com.luc.qa.module.user.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicProfileAnswerDTO {

    private Long id;
    private Long questionId;
    private String questionTitle;
    private String bodyPreview;
    private int score;
    private Instant createdAt;
}
