package org.auwerk.otus.arch.userservice.service.impl;

import javax.enterprise.context.ApplicationScoped;

import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.exception.KeycloakIntegrationException;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapper;
import org.auwerk.otus.arch.userservice.service.KeycloakService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;

@ApplicationScoped
public class KeycloakServiceImpl implements KeycloakService {

    private final Vertx vertx;
    private final Keycloak keycloak;
    private final UserProfileMapper userProfileMapper;
    private final String keycloakRealm;

    public KeycloakServiceImpl(Vertx vertx, Keycloak keycloak, UserProfileMapper userProfileMapper,
            @ConfigProperty(name = "otus.keycloak.realm") String keycloakRealm) {
        this.vertx = vertx;
        this.keycloak = keycloak;
        this.userProfileMapper = userProfileMapper;
        this.keycloakRealm = keycloakRealm;
    }

    @Override
    public Uni<Void> createUser(UserProfile profile) {
        return vertx.getOrCreateContext().executeBlocking(
                Uni.createFrom().emitter(emitter -> {
                    final var userRepresentation = new UserRepresentation();
                    userRepresentation.setEnabled(true);
                    userRepresentation.setEmailVerified(true);

                    userProfileMapper.updateUserRepresentationFromProfile(profile, userRepresentation);

                    final var response = keycloak.realm(keycloakRealm).users()
                            .create(userRepresentation);

                    if (response.getStatus() != 201) {
                        emitter.fail(new KeycloakIntegrationException("user account creation failed"));
                    }

                    emitter.complete(profile);
                }).replaceWithVoid());
    }

    @Override
    public Uni<Void> setUserPassword(UserProfile profile, String password) {
        return vertx.getOrCreateContext().executeBlocking(Uni.createFrom().emitter(emitter -> {
            final var realm = keycloak.realm(keycloakRealm);

            final var userProfiles = realm.users()
                    .searchByUsername(profile.getUserName(), true);
            if (userProfiles.isEmpty()) {
                emitter.fail(new KeycloakIntegrationException("user account not found"));
            }

            final var passwordCredential = new CredentialRepresentation();
            passwordCredential.setId(CredentialRepresentation.PASSWORD);
            passwordCredential.setValue(password);

            realm.users().get(userProfiles.get(0).getId()).resetPassword(passwordCredential);

            emitter.complete(profile);
        }).replaceWithVoid());
    }
}
