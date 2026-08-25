package com.luc.qa.module.course.mapper;

import com.luc.qa.module.course.dto.CourseSummaryDTO;
import com.luc.qa.module.course.dto.TopicSummaryDTO;
import com.luc.qa.module.course.entity.Course;
import com.luc.qa.module.topic.entity.Topic;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    CourseSummaryDTO toSummary(Course course);

    TopicSummaryDTO toTopicSummary(Topic topic);
}
