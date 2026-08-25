package com.luc.qa.module.user.dto;

import com.luc.qa.module.alumni.entity.Degree;
import com.luc.qa.module.alumni.entity.Faculty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicAlumniInfoDTO {

    private Integer gradYear;
    private Faculty faculty;
    private Degree degree;
    private String major;
    private String currentPosition;
    private String currentCompany;
    private String linkedinUrl;
}
