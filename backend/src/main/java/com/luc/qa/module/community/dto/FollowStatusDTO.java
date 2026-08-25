package com.luc.qa.module.community.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowStatusDTO {

    private boolean following;
    private long followerCount;
}
