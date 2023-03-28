package org.auwerk.otus.arch.userservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.NoSuchElementException;

import org.auwerk.otus.arch.userservice.dao.UserProfileDao;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.exception.KeycloakIntegrationException;
import org.auwerk.otus.arch.userservice.exception.UserProfileNotFoundException;
import org.auwerk.otus.arch.userservice.service.KeycloakService;
import org.auwerk.otus.arch.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.pgclient.PgPool;

public class UserServiceImplTest {

    private static final String USERNAME = "user";

    private final PgPool pool = mock(PgPool.class);
    private final SecurityIdentity securityIdentity = mock(SecurityIdentity.class);
    private final UserProfileDao userProfileDao = mock(UserProfileDao.class);
    private final KeycloakService keycloakService = mock(KeycloakService.class);
    private final UserService userService = new UserServiceImpl(pool, securityIdentity, userProfileDao,
            keycloakService);

    @BeforeEach
    void mockSecurityIdentity() {
        final var principal = mock(Principal.class);
        when(principal.getName()).thenReturn(USERNAME);
        when(securityIdentity.getPrincipal()).thenReturn(principal);
    }

    @Test
    void createUser_success() {
        // given
        final var initialPassword = "password";
        final var userProfile = new UserProfile();

        // when
        when(keycloakService.createUser(userProfile))
                .thenReturn(Uni.createFrom().voidItem());
        when(keycloakService.setUserPassword(userProfile, initialPassword))
                .thenReturn(Uni.createFrom().voidItem());
        final var subscriber = userService.createUser(userProfile, initialPassword).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertCompleted();

        verify(keycloakService, times(1)).createUser(userProfile);
        verify(keycloakService, times(1)).setUserPassword(userProfile, initialPassword);
    }

    @Test
    void createUser_keycloakCreateUserAccountFailed() {
        // given
        final var initialPassword = "password";
        final var userProfile = new UserProfile();
        final var ex = new KeycloakIntegrationException("");

        // when
        when(keycloakService.createUser(userProfile))
                .thenReturn(Uni.createFrom().failure(ex));
        when(keycloakService.setUserPassword(userProfile, initialPassword))
                .thenReturn(Uni.createFrom().voidItem());
        final var subscriber = userService.createUser(userProfile, initialPassword).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = subscriber
                .assertFailedWith(KeycloakIntegrationException.class)
                .getFailure();
        assertSame(ex, failure);

        verify(keycloakService, times(1)).createUser(userProfile);
        verify(keycloakService, never()).setUserPassword(userProfile, initialPassword);
    }

    @Test
    void createUser_keycloakSetUserPasswordFailed() {
        // given
        final var initialPassword = "password";
        final var userProfile = new UserProfile();
        final var ex = new KeycloakIntegrationException("");

        // when
        when(keycloakService.createUser(userProfile))
                .thenReturn(Uni.createFrom().voidItem());
        when(keycloakService.setUserPassword(userProfile, initialPassword))
                .thenReturn(Uni.createFrom().failure(ex));
        final var subscriber = userService.createUser(userProfile, initialPassword).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = subscriber
                .assertFailedWith(KeycloakIntegrationException.class)
                .getFailure();
        assertSame(ex, failure);

        verify(keycloakService, times(1)).createUser(userProfile);
        verify(keycloakService, times(1)).setUserPassword(userProfile, initialPassword);
    }

    @Test
    void getMyProfile_success() {
        // given
        final var userProfile = new UserProfile();

        // when
        when(userProfileDao.findByUserName(pool, USERNAME))
                .thenReturn(Uni.createFrom().item(userProfile));
        final var subscriber = userService.getMyProfile().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertItem(userProfile);
    }

    @Test
    void getMyProfile_userNotFound() {
        // when
        when(userProfileDao.findByUserName(pool, USERNAME))
                .thenReturn(Uni.createFrom().failure(new NoSuchElementException()));
        final var subscriber = userService.getMyProfile().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = (UserProfileNotFoundException) subscriber
                .assertFailedWith(UserProfileNotFoundException.class)
                .getFailure();
        assertEquals(USERNAME, failure.getUserName());
    }

    @Test
    void getUserProfile_success() {
        // given
        final var userId = 1L;
        final var userProfile = new UserProfile();

        // when
        when(userProfileDao.findById(pool, userId))
                .thenReturn(Uni.createFrom().item(userProfile));
        final var subscriber = userService.getUserProfile(userId).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertItem(userProfile);
    }

    @Test
    void getUserProfile_userNotFound() {
        // given
        final var userId = 1L;

        // when
        when(userProfileDao.findById(pool, userId))
                .thenReturn(Uni.createFrom().failure(new NoSuchElementException()));
        final var subscriber = userService.getUserProfile(userId).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = (UserProfileNotFoundException) subscriber
                .assertFailedWith(UserProfileNotFoundException.class)
                .getFailure();
        assertEquals(userId, failure.getUserId());
    }

    @Test
    void updateMyProfile_success() {
        // given
        final var userProfile = new UserProfile();

        // when
        when(userProfileDao.updateByUserName(pool, USERNAME, userProfile))
                .thenReturn(Uni.createFrom().voidItem());
        final var subscriber = userService.updateMyProfile(userProfile).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertCompleted();

        verify(userProfileDao, only())
                .updateByUserName(pool, USERNAME, userProfile);
    }
}
