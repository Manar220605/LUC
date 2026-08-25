package com.luc.qa.common.exception;

public class MentorshipRequestAlreadyOpenException extends ConflictException {

    public MentorshipRequestAlreadyOpenException() {
        super("You already have a pending mentorship request with this alumnus");
    }
}
