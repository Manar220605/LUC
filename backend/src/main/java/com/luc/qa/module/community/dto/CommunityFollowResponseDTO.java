package com.luc.qa.module.community.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommunityFollowResponseDTO {

    private CommunityResponseDTO community;
    private Instant followedAt;
}
