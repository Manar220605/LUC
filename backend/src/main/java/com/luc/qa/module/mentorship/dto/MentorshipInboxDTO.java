package com.luc.qa.module.mentorship.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MentorshipInboxDTO {

    private List<MentorshipRequestResponseDTO> incoming;
    private List<MentorshipRequestResponseDTO> outgoing;
}
