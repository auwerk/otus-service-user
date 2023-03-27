package org.auwerk.otus.arch.userservice.exception;

import lombok.Getter;

public class KeycloakIntegrationException extends RuntimeException {

    @Getter
    private final int status;

    public KeycloakIntegrationException(int status) {
        super("keycloak integration failure, status=" + status);
        this.status = status;
    }
}
