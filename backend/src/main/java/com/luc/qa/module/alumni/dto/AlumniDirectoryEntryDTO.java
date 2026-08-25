package com.luc.qa.module.alumni.dto;

import com.luc.qa.module.alumni.entity.Degree;
import com.luc.qa.module.alumni.entity.Faculty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlumniDirectoryEntryDTO {

    private Long userId;
    private String displayName;
    private String avatarUrl;
    private Integer gradYear;
    private Faculty faculty;
    private Degree degree;
    private String major;
    private String currentPosition;
    private String currentCompany;
    private String linkedinUrl;
}
