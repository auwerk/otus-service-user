package org.auwerk.otus.arch.userservice.exception;

public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super("dao error: " + message);
    }
}
