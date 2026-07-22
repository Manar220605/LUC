package com.luc.qa.common.exception;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
