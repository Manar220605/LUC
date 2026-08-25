package com.luc.qa.module.course.service;

import com.luc.qa.module.course.dto.CourseDetailDTO;
import com.luc.qa.module.course.dto.CourseSummaryDTO;
import java.util.List;

public interface CourseService {

    List<CourseSummaryDTO> listAll();

    CourseDetailDTO getByCode(String code, String keycloakId);
}
