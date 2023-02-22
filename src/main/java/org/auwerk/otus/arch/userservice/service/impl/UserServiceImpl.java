package org.auwerk.otus.arch.userservice.service.impl;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.auwerk.otus.arch.userservice.dao.UserProfileDao;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.service.UserService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    private final Vertx vertx;
    private final Keycloak keycloak;
    private final UserProfileDao userProfileDao;
    private final String keycloakRealm;

    public UserServiceImpl(Vertx vertx, Keycloak keycloak, UserProfileDao userProfileDao,
                           @ConfigProperty(name = "otus.service.user.keycloak-realm") String keycloakRealm) {
        this.vertx = vertx;
        this.keycloak = keycloak;
        this.userProfileDao = userProfileDao;
        this.keycloakRealm = keycloakRealm;
    }

    @Override
    public Uni<Long> createUser(UserProfile profile) {
        return createUserInKeycloak(profile)
                .onItem().transformToUni(p -> userProfileDao.insert(p));
    }

    private Uni<UserProfile> createUserInKeycloak(UserProfile profile) {
        return vertx.getOrCreateContext().executeBlocking(
                Uni.createFrom().emitter(emitter -> {
                    final var response = keycloak.realm(keycloakRealm).users()
                            .create(profileToRepresentation(profile));

                    if (response.getStatus() == 201) {
                        emitter.complete(profile);
                    } else {
                        emitter.fail(new Exception("failed to create user in keycloak"));
                    }
                }));
    }

    private static UserRepresentation profileToRepresentation(UserProfile profile) {
        final var userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(profile.getUserName());
        userRepresentation.setEmail(profile.getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setFirstName(profile.getFirstName());
        userRepresentation.setLastName(profile.getLastName());
        return userRepresentation;
    }
}
