package org.auwerk.otus.arch.userservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.Response;

import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.exception.KeycloakIntegrationException;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapper;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapperImpl;
import org.auwerk.otus.arch.userservice.service.KeycloakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.Vertx;

public class KeycloakServiceImplTest {

    private static final String KEYCLOAK_REALM = "test-realm";

    private final Vertx vertx = Vertx.vertx();
    private final Keycloak keycloak = mock(Keycloak.class);
    private final UserProfileMapper userProfileMapper = new UserProfileMapperImpl();
    private final KeycloakService keycloakService = new KeycloakServiceImpl(vertx, keycloak, userProfileMapper,
            KEYCLOAK_REALM);

    private final RealmResource realmResource = mock(RealmResource.class);
    private final UsersResource usersResource = mock(UsersResource.class);

    @BeforeEach
    void mockKeycloak() {
        when(realmResource.users()).thenReturn(usersResource);
        when(keycloak.realm(KEYCLOAK_REALM)).thenReturn(realmResource);
    }

    @Test
    void createUser_success() {
        // given
        final var userProfile = new UserProfile();

        // when
        when(usersResource.create(any(UserRepresentation.class)))
                .thenReturn(Response.status(201).build());
        final var subscriber = keycloakService.createUser(userProfile).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        vertx.closeAndAwait(); // run blocking code
        subscriber.assertCompleted();

        verify(usersResource, times(1))
                .create(argThat(ur -> ur.isEnabled() && ur.isEmailVerified()));
    }

    @Test
    void createUser_failure() {
        // given
        final var userProfile = new UserProfile();

        // when
        when(usersResource.create(any(UserRepresentation.class)))
                .thenReturn(Response.status(500).build());
        final var subscriber = keycloakService.createUser(userProfile).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        vertx.closeAndAwait(); // run blocking code
        final var failure = subscriber
                .assertFailedWith(KeycloakIntegrationException.class)
                .getFailure();
        assertEquals("user account creation failed", failure.getMessage());
    }

    @Test
    void setUserPassword_success() {
        // given
        final var userId = "some-id";
        final var userName = "user";
        final var password = "password";
        final var userProfile = new UserProfile();
        final var userRepresentation = new UserRepresentation();
        final var userResource = mock(UserResource.class);

        // when
        userProfile.setUserName(userName);
        userRepresentation.setId(userId);
        when(usersResource.searchByUsername(userName, true))
                .thenReturn(List.of(userRepresentation));
        when(usersResource.get(userId))
                .thenReturn(userResource);
        final var subscriber = keycloakService.setUserPassword(userProfile, password).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        vertx.closeAndAwait(); // run blocking code
        subscriber.assertCompleted();

        verify(userResource, times(1))
                .resetPassword(argThat(
                        cr -> CredentialRepresentation.PASSWORD.equals(cr.getId()) && password.equals(cr.getValue())));
    }

    @Test
    void setUserPassword_userNotFound() {
        // given
        final var userId = "some-id";
        final var userName = "user";
        final var password = "password";
        final var userProfile = new UserProfile();
        final var userResource = mock(UserResource.class);

        // when
        userProfile.setUserName(userName);
        when(usersResource.searchByUsername(userName, true))
                .thenReturn(List.of());
        when(usersResource.get(userId))
                .thenReturn(userResource);
        final var subscriber = keycloakService.setUserPassword(userProfile, password).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        vertx.closeAndAwait(); // run blocking code
        final var failure = subscriber
                .assertFailedWith(KeycloakIntegrationException.class)
                .getFailure();
        assertEquals("user account not found", failure.getMessage());
    }
}
