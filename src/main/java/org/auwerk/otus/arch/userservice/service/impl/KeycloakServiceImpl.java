package org.auwerk.otus.arch.userservice.service.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.exception.KeycloakIntegrationException;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapper;
import org.auwerk.otus.arch.userservice.service.KeycloakService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
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
                        emitter.fail(new KeycloakIntegrationException(
                                "user account creation failed, status=" + response.getStatus()));
                    }

                    emitter.complete(response);
                }).replaceWithVoid());
    }

    @Override
    public Uni<Void> deleteUserAccount(UserProfile profile) {
        return vertx.getOrCreateContext().executeBlocking(Uni.createFrom().emitter(emitter -> {
            try {
                final var realm = keycloak.realm(keycloakRealm);
                final var userId = getUserIdByName(realm, profile.getUserName())
                        .orElseThrow(() -> new KeycloakIntegrationException("user account not found"));

                final var response = realm.users().delete(userId);
                if (response.getStatus() != 200) {
                    emitter.fail(new KeycloakIntegrationException(
                            "user account deletion failed, status=" + response.getStatus()));
                }

                emitter.complete(response);
            } catch (Throwable t) {
                emitter.fail(t);
            }
        }).replaceWithVoid());
    }

    @Override
    public Uni<Void> setUserPassword(UserProfile profile, String password) {
        return vertx.getOrCreateContext().executeBlocking(Uni.createFrom().emitter(emitter -> {
            try {
                final var realm = keycloak.realm(keycloakRealm);
                final var userId = getUserIdByName(realm, profile.getUserName())
                        .orElseThrow(() -> new KeycloakIntegrationException("user account not found"));

                final var passwordCredential = new CredentialRepresentation();
                passwordCredential.setId(CredentialRepresentation.PASSWORD);
                passwordCredential.setValue(password);

                realm.users().get(userId).resetPassword(passwordCredential);

                emitter.complete(profile);
            } catch (Throwable t) {
                emitter.fail(t);
            }
        }).replaceWithVoid());
    }

    private static Optional<String> getUserIdByName(RealmResource realm, String userName) {
        final var userProfiles = realm.users().searchByUsername(userName, true);
        if (userProfiles.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(userProfiles.get(0).getId());
    }
}
