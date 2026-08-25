package com.luc.qa.common.exception;

public class MentorshipAlreadyAcceptedException extends ConflictException {

    public MentorshipAlreadyAcceptedException() {
        super("This alumnus already accepted a mentorship request from you");
    }
}
