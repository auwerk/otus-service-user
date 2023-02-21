package org.auwerk.otus.arch.userservice.service.impl;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.auwerk.otus.arch.userservice.service.UserService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    private final Vertx vertx;
    private final Keycloak keycloak;

    public UserServiceImpl(Vertx vertx, Keycloak keycloak) {
        this.vertx = vertx;
        this.keycloak = keycloak;
    }

    @Override
    public Uni<Long> createUser(String userName, String email) {
        return vertx.getOrCreateContext().executeBlocking(
                Uni.createFrom().emitter(emitter -> {
                    final var userRepresentation = new UserRepresentation();
                    userRepresentation.setUsername(userName);
                    userRepresentation.setEmail(email);
                    userRepresentation.setEnabled(true);
                    userRepresentation.setEmailVerified(true);

                    final var response = keycloak.realm("otus").users().create(userRepresentation);
                    if (response.getStatus() == 201) {
                        emitter.complete(1L);
                    } else {
                        emitter.complete(null);
                    }
                }));
    }
}
