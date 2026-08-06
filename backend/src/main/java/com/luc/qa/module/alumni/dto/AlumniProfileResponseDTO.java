package com.luc.qa.module.alumni.dto;

import com.luc.qa.module.alumni.entity.Degree;
import com.luc.qa.module.alumni.entity.Faculty;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlumniProfileResponseDTO {

    private int gradYear;
    private Faculty faculty;
    private Degree degree;
    private String major;
    private String currentPosition;
    private String currentCompany;
    private String linkedinUrl;
    private boolean isPublic;
    private Instant createdAt;
    private Instant updatedAt;
}
