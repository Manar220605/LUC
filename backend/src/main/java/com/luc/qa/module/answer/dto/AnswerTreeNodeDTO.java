package com.luc.qa.module.answer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.luc.qa.module.user.dto.PublicAuthorDTO;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AnswerTreeNodeDTO {

    private Long id;
    private String body;
    private PublicAuthorDTO author;
    private boolean anonymous;
    private boolean deleted;
    private int score;
    private Instant createdAt;
    private Instant updatedAt;

    @JsonProperty("ownedByCurrentUser")
    private boolean ownedByCurrentUser;

    private Integer viewerVote;

    @Builder.Default
    private List<AnswerTreeNodeDTO> replies = new ArrayList<>();
}
