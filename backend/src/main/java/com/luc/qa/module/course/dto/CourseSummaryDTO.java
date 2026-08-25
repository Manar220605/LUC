package com.luc.qa.module.course.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseSummaryDTO {

    private String code;
    private String title;
    private int yearLevel;
    private int semester;
    private int credits;
}
