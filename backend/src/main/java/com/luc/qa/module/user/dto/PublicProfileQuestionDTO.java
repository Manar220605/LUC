package com.luc.qa.module.user.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicProfileQuestionDTO {

    private Long id;
    private String title;
    private String communityPath;
    private String communityName;
    private int score;
    private int answerCount;
    private Instant createdAt;
}
