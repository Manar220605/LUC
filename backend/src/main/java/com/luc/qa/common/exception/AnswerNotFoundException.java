package com.luc.qa.common.exception;

public class AnswerNotFoundException extends NotFoundException {

    public AnswerNotFoundException(Long id) {
        super("Answer not found: " + id);
    }
}
