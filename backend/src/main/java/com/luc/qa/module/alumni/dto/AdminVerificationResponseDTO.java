package com.luc.qa.module.alumni.dto;

import com.luc.qa.module.alumni.entity.Degree;
import com.luc.qa.module.alumni.entity.Faculty;
import com.luc.qa.module.alumni.entity.VerificationStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminVerificationResponseDTO {

    private Long id;
    private Long userId;
    private String userDisplayName;
    private String userEmail;
    private String linkedinUrl;
    private int claimedGradYear;
    private Faculty claimedFaculty;
    private Degree claimedDegree;
    private String claimedMajor;
    private String claimedPosition;
    private String claimedCompany;
    private VerificationStatus status;
    private Instant submittedAt;
    private Instant reviewedAt;
    private String rejectionReason;
}
