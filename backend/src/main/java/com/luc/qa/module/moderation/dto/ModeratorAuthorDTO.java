package com.luc.qa.module.moderation.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ModeratorAuthorDTO {

    Long userId;
    String displayName;
    String email;
    boolean postedAnonymously;
}
