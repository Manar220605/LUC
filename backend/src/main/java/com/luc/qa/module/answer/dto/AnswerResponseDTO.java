package com.luc.qa.module.answer.dto;

import com.luc.qa.module.user.dto.PublicAuthorDTO;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class AnswerResponseDTO {

    private Long id;
    private Long questionId;
    private Long parentAnswerId;
    private String body;
    private PublicAuthorDTO author;
    private boolean anonymous;
    private boolean deleted;
    private int score;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean ownedByCurrentUser;
}
