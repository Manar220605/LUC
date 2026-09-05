package com.luc.qa.module.course.dto;

import com.luc.qa.module.question.dto.QuestionSummaryDTO;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseDetailDTO {

    private CourseSummaryDTO course;
    private String description;
    private List<TopicSummaryDTO> topics;
    private List<QuestionSummaryDTO> linkedQuestions;
    private List<CourseExpertDTO> people;
}
