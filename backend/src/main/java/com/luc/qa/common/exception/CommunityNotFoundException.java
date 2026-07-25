package com.luc.qa.common.exception;

public class CommunityNotFoundException extends NotFoundException {

    public CommunityNotFoundException(String path) {
        super("Community not found: " + path);
    }

    public CommunityNotFoundException(Long id) {
        super("Community not found: id " + id);
    }
}
