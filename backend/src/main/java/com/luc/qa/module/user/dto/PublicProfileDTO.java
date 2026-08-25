package com.luc.qa.module.user.dto;

import com.luc.qa.module.user.entity.UserRole;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicProfileDTO {

    private Long id;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private UserRole role;
    private Instant createdAt;
    private long questionCount;
    private long answerCount;
    private long score;
    private PublicAlumniInfoDTO alumni;
    private List<PublicProfileQuestionDTO> questions;
    private List<PublicProfileAnswerDTO> answers;
}
