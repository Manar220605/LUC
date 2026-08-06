package com.luc.qa.common.exception;

public class VerificationNotFoundException extends NotFoundException {

    public VerificationNotFoundException(Long id) {
        super("Verification request not found: " + id);
    }

    public VerificationNotFoundException(String message) {
        super(message);
    }
}
