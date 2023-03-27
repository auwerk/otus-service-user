package org.auwerk.otus.arch.userservice.exception;

import lombok.Getter;

public class UserProfileNotFoundException extends RuntimeException {

    @Getter
    private final Long userId;
    @Getter
    private final String userName;

    public UserProfileNotFoundException(Long userId) {
        super("user profile not found, id=" + userId);
        this.userId = userId;
        this.userName = null;
    }

    public UserProfileNotFoundException(String userName) {
        super("user profile not found, userName=" + userName);
        this.userId = null;
        this.userName = userName;
    }
}
