package com.luc.qa.common.exception;

public class AnnouncementNotFoundException extends NotFoundException {

    public AnnouncementNotFoundException(Long id) {
        super("Announcement not found: " + id);
    }
}
