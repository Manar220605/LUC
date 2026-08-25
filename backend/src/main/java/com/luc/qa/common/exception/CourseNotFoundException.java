package com.luc.qa.common.exception;

public class CourseNotFoundException extends NotFoundException {

    public CourseNotFoundException(String code) {
        super("Course not found: " + code);
    }
}
