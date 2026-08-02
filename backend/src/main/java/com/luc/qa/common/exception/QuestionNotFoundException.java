package com.luc.qa.common.exception;

public class QuestionNotFoundException extends NotFoundException {

    public QuestionNotFoundException(Long id) {
        super("Question not found: " + id);
    }
}
