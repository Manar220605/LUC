package com.luc.qa.module.vote.dto;

import com.luc.qa.module.vote.entity.VoteTargetType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoteResponseDTO {

    private VoteTargetType targetType;
    private Long targetId;
    private Integer viewerVote;
    private int score;
}
