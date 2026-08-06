package com.luc.qa.common.exception;

public class ReportNotFoundException extends NotFoundException {

    public ReportNotFoundException(Long id) {
        super("Report not found: " + id);
    }
}
