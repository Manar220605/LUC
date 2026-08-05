package com.luc.qa.module.vote.dto;

import com.luc.qa.module.vote.entity.VoteTargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CastVoteRequestDTO {

    @NotNull
    private VoteTargetType targetType;

    @NotNull
    private Long targetId;

    @NotNull
    private Integer value;
}
