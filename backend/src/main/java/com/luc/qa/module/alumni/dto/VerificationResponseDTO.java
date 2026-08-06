package com.luc.qa.module.alumni.dto;

import com.luc.qa.module.alumni.entity.Degree;
import com.luc.qa.module.alumni.entity.Faculty;
import com.luc.qa.module.alumni.entity.VerificationStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerificationResponseDTO {

    private Long id;
    private VerificationStatus status;
    private String linkedinUrl;
    private int claimedGradYear;
    private Faculty claimedFaculty;
    private Degree claimedDegree;
    private String claimedMajor;
    private String claimedPosition;
    private String claimedCompany;
    private Instant submittedAt;
    private Instant reviewedAt;
    private String rejectionReason;
}
