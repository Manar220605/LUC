package com.luc.qa.module.course.repository;

import com.luc.qa.module.course.entity.Course;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @EntityGraph(attributePaths = "topics")
    Optional<Course> findWithTopicsByCodeIgnoreCase(String code);

    List<Course> findAllByOrderByYearLevelAscSemesterAscCodeAsc();
}
