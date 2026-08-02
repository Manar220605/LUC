package com.luc.qa.module.question.dto;

import com.luc.qa.module.community.dto.CommunityResponseDTO;
import com.luc.qa.module.question.entity.QuestionStatus;
import com.luc.qa.module.user.dto.PublicAuthorDTO;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionResponseDTO {

    private Long id;
    private String title;
    private String body;
    private PublicAuthorDTO author;
    private CommunityResponseDTO community;
    private QuestionStatus status;
    private boolean anonymous;
    private int viewCount;
    private int answerCount;
    private int score;
    private Instant createdAt;
    private Instant updatedAt;
}
