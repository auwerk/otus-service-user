package org.auwerk.otus.arch.userservice.service.exception;

import lombok.Getter;

public class UserProfileNotFoundException extends Exception {

    @Getter
    private final Long userId;

    public UserProfileNotFoundException(Long userId) {
        super("user profile not found, id=" + userId);
        this.userId = userId;
    }
}
