package com.luc.qa.module.mentorship.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MentorshipStatusDTO {

    private boolean canRequest;
    private String cannotRequestReason;
    private MentorshipRequestResponseDTO existing;
}
