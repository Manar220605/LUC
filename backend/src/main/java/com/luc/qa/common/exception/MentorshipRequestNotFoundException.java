package com.luc.qa.common.exception;

public class MentorshipRequestNotFoundException extends NotFoundException {

    public MentorshipRequestNotFoundException(Long id) {
        super("Mentorship request not found: " + id);
    }
}
