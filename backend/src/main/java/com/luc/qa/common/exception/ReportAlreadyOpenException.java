package com.luc.qa.common.exception;

public class ReportAlreadyOpenException extends ConflictException {

    public ReportAlreadyOpenException() {
        super("You already have an open report for this target");
    }
}
