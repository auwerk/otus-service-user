package org.auwerk.otus.arch.userservice.exception;

import lombok.Getter;

public class UserProfileNotFoundException extends RuntimeException {

    @Getter
    private final String userName;

    public UserProfileNotFoundException(String userName) {
        super("user profile not found, userName=" + userName);
        this.userName = userName;
    }
}
