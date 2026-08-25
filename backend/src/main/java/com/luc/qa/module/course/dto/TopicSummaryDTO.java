package com.luc.qa.module.course.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopicSummaryDTO {

    private String slug;
    private String name;
    private String kind;
}
