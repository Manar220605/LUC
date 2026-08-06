package com.luc.qa.common.exception;

public class VerificationAlreadyPendingException extends ConflictException {

    public VerificationAlreadyPendingException() {
        super("You already have a pending alumni verification request");
    }
}
