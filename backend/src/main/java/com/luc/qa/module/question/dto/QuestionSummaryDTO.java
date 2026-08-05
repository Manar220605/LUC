package com.luc.qa.module.question.dto;

import com.luc.qa.module.user.dto.PublicAuthorDTO;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class QuestionSummaryDTO {

    private Long id;
    private String title;
    private PublicAuthorDTO author;
    private String communityPath;
    private String communityName;
    private int score;
    private int answerCount;
    private int viewCount;
    private Instant createdAt;
    private Integer viewerVote;
    private boolean ownedByCurrentUser;
}
