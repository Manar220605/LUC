package com.luc.qa.module.course.dto;

import com.luc.qa.module.user.dto.PublicAuthorDTO;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseExpertDTO {

    private PublicAuthorDTO author;
    private boolean alumni;
    private String currentCompany;
    private double weight;
}
