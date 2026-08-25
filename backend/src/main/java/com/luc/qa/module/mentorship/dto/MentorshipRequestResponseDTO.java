package com.luc.qa.module.mentorship.dto;

import com.luc.qa.module.mentorship.entity.MentorshipRequestStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MentorshipRequestResponseDTO {

    private Long id;
    private Long studentId;
    private String studentDisplayName;
    private String studentAvatarUrl;
    private Long alumniId;
    private String alumniDisplayName;
    private String alumniAvatarUrl;
    private String alumniLinkedinUrl;
    private String message;
    private MentorshipRequestStatus status;
    private Instant createdAt;
    private Instant respondedAt;
}
