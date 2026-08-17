package com.luc.qa.common.exception;

public class NotificationNotFoundException extends NotFoundException {

    public NotificationNotFoundException(Long id) {
        super("Notification not found: " + id);
    }
}
