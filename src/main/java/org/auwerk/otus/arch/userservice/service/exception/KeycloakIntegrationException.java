package org.auwerk.otus.arch.userservice.service.exception;

import lombok.Getter;

public class KeycloakIntegrationException extends Exception {

    @Getter
    private final int status;

    public KeycloakIntegrationException(int status) {
        super("keycloak integration failure, status=" + status);
        this.status = status;
    }
}
