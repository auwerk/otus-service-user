package org.auwerk.otus.arch.userservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;

import org.auwerk.otus.arch.userservice.dao.UserProfileDao;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.exception.KeycloakIntegrationException;
import org.auwerk.otus.arch.userservice.exception.UserProfileNotFoundException;
import org.auwerk.otus.arch.userservice.service.BillingService;
import org.auwerk.otus.arch.userservice.service.KeycloakService;
import org.auwerk.otus.arch.userservice.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.SqlConnection;

public class UserProfileServiceImplTest {

    private static final String USERNAME = "user";
    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    private final PgPool pool = mock(PgPool.class);
    private final SecurityIdentity securityIdentity = mock(SecurityIdentity.class);
    private final UserProfileDao userProfileDao = mock(UserProfileDao.class);
    private final KeycloakService keycloakService = mock(KeycloakService.class);
    private final BillingService billingService = mock(BillingService.class);
    private final UserProfileService userProfileService = new UserProfileServiceImpl(pool, securityIdentity,
            userProfileDao, keycloakService, billingService);

    @BeforeEach
    void mockTransaction() {
        when(pool.withTransaction(any()))
        .then(inv -> {
            final Function<SqlConnection, Uni<UserProfile>> f = inv.getArgument(0);
            return f.apply(null);
        });
    }

    @BeforeEach
    void mockSecurityIdentity() {
        final var principal = mock(Principal.class);
        when(principal.getName()).thenReturn(USERNAME);
        when(securityIdentity.getPrincipal()).thenReturn(principal);
    }

    @Test
    void createUserProfile_success() {
        // given
        final var userId = 1L;
        final var initialPassword = "password";
        final var userProfile = new UserProfile();
        userProfile.setUserName(USERNAME);

        // when
        when(billingService.createUserAccount(USERNAME))
                .thenReturn(Uni.createFrom().item(ACCOUNT_ID));
        when(keycloakService.createUserAccount(userProfile))
                .thenReturn(Uni.createFrom().voidItem());
        when(keycloakService.setUserPassword(userProfile, initialPassword))
                .thenReturn(Uni.createFrom().voidItem());
        when(userProfileDao.insert(pool, userProfile))
                .thenReturn(Uni.createFrom().item(userId));
        final var subscriber = userProfileService.createUserProfile(userProfile, initialPassword).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertItem(userId);

        verify(keycloakService, times(1)).createUserAccount(userProfile);
        verify(keycloakService, times(1)).setUserPassword(userProfile, initialPassword);
        verify(userProfileDao, times(1)).insert(pool, userProfile);
    }

    @Test
    void createUserProfile_keycloakCreateUserAccountFailed() {
        // given
        final var profileId = 10L;
        final var initialPassword = "password";
        final var userProfile = buildUserProfile();
        final var ex = new KeycloakIntegrationException("");

        // when
        when(keycloakService.createUserAccount(userProfile))
                .thenReturn(Uni.createFrom().failure(ex));
        when(keycloakService.setUserPassword(userProfile, initialPassword))
                .thenReturn(Uni.createFrom().voidItem());
        when(billingService.deleteUserAccount(userProfile.getUserName()))
                .thenReturn(Uni.createFrom().voidItem());
        when(userProfileDao.insert(pool, userProfile))
                .thenReturn(Uni.createFrom().item(profileId));
        final var subscriber = userProfileService.createUserProfile(userProfile, initialPassword).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = subscriber
                .assertFailedWith(KeycloakIntegrationException.class)
                .getFailure();
        assertSame(ex, failure);

        verify(keycloakService, times(1)).createUserAccount(userProfile);
        verify(keycloakService, times(1)).setUserPassword(userProfile, initialPassword);
        verify(billingService, times(1)).createUserAccount(userProfile.getUserName());
        verify(userProfileDao, times(1)).insert(pool, userProfile);

        verify(userProfileDao, times(1)).deleteById(pool, profileId);
        verify(billingService, times(1)).deleteUserAccount(userProfile.getUserName());
        verify(keycloakService, times(1)).deleteUserAccount(userProfile);
    }

    @Test
    void createUserProfile_keycloakSetUserPasswordFailed() {
        // given
        final var profileId = 10L;
        final var initialPassword = "password";
        final var userProfile = buildUserProfile();
        final var ex = new KeycloakIntegrationException("");

        // when
        when(keycloakService.createUserAccount(userProfile))
                .thenReturn(Uni.createFrom().voidItem());
        when(keycloakService.setUserPassword(userProfile, initialPassword))
                .thenReturn(Uni.createFrom().failure(ex));
        when(billingService.deleteUserAccount(userProfile.getUserName()))
                .thenReturn(Uni.createFrom().voidItem());
        when(userProfileDao.insert(pool, userProfile))
                .thenReturn(Uni.createFrom().item(profileId));
        final var subscriber = userProfileService.createUserProfile(userProfile, initialPassword).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = subscriber
                .assertFailedWith(KeycloakIntegrationException.class)
                .getFailure();
        assertSame(ex, failure);

        verify(keycloakService, times(1)).createUserAccount(userProfile);
        verify(keycloakService, times(1)).setUserPassword(userProfile, initialPassword);
        verify(billingService, times(1)).createUserAccount(userProfile.getUserName());
        verify(userProfileDao, times(1)).insert(pool, userProfile);

        verify(userProfileDao, times(1)).deleteById(pool, profileId);
        verify(billingService, times(1)).deleteUserAccount(userProfile.getUserName());
        verify(keycloakService, times(1)).deleteUserAccount(userProfile);
    }

    @Test
    void getMyProfile_success() {
        // given
        final var userProfile = new UserProfile();

        // when
        when(userProfileDao.findByUserName(pool, USERNAME))
                .thenReturn(Uni.createFrom().item(userProfile));
        final var subscriber = userProfileService.getMyProfile().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertItem(userProfile);
    }

    @Test
    void getMyProfile_userProfileNotFound() {
        // when
        when(userProfileDao.findByUserName(pool, USERNAME))
                .thenReturn(Uni.createFrom().failure(new NoSuchElementException()));
        final var subscriber = userProfileService.getMyProfile().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = (UserProfileNotFoundException) subscriber
                .assertFailedWith(UserProfileNotFoundException.class)
                .getFailure();
        assertEquals(USERNAME, failure.getUserName());
    }

    @Test
    void updateMyProfile_success() {
        // given
        final var userProfile = buildUserProfile();
        final var userProfileUpdate = new UserProfile();

        // when
        when(userProfileDao.findByUserName(pool, USERNAME))
                .thenReturn(Uni.createFrom().item(userProfile));
        when(userProfileDao.updateById(pool, userProfile.getId(), userProfile))
                .thenReturn(Uni.createFrom().voidItem());
        final var subscriber = userProfileService.updateMyProfile(userProfileUpdate).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertCompleted();

        verify(userProfileDao, times(1))
                .updateById(pool, userProfile.getId(), userProfileUpdate);
    }

    @Test
    void updateMyProfile_userProfileNotFound() {
        // given
        final var userProfileUpdate = buildUserProfile();

        // when
        userProfileUpdate.setUserName(USERNAME);
        when(userProfileDao.findByUserName(pool, USERNAME))
                .thenReturn(Uni.createFrom().failure(new NoSuchElementException()));
        final var subscriber = userProfileService.updateMyProfile(userProfileUpdate).subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = (UserProfileNotFoundException) subscriber
                .assertFailedWith(UserProfileNotFoundException.class)
                .getFailure();
        assertEquals(USERNAME, failure.getUserName());

        verify(userProfileDao, never())
                .updateById(same(pool), anyLong(), same(userProfileUpdate));
    }

    @Test
    void deleteMyProfile_success() {
        // given
        final var userProfile = buildUserProfile();

        // when
        when(userProfileDao.findByUserName(pool, USERNAME))
                .thenReturn(Uni.createFrom().item(userProfile));
        when(userProfileDao.deleteById(pool, userProfile.getId()))
                .thenReturn(Uni.createFrom().voidItem());
        when(keycloakService.deleteUserAccount(userProfile))
                .thenReturn(Uni.createFrom().voidItem());
        final var subscriber = userProfileService.deleteMyProfile().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        subscriber.assertCompleted();

        verify(billingService, times(1)).deleteUserAccount(USERNAME);
        verify(keycloakService, times(1)).deleteUserAccount(userProfile);
        verify(userProfileDao, times(1)).deleteById(pool, userProfile.getId());
    }

    @Test
    void deleteMyProfile_userProfileNotFound() {
        // when
        when(userProfileDao.findByUserName(pool, USERNAME))
                .thenReturn(Uni.createFrom().failure(new NoSuchElementException()));
        final var subscriber = userProfileService.deleteMyProfile().subscribe()
                .withSubscriber(UniAssertSubscriber.create());

        // then
        final var failure = (UserProfileNotFoundException) subscriber
                .assertFailedWith(UserProfileNotFoundException.class)
                .getFailure();
        assertEquals(USERNAME, failure.getUserName());

        verify(userProfileDao, never())
                .deleteById(same(pool), anyLong());
    }

    private static UserProfile buildUserProfile() {
        final var userProfile = new UserProfile();
        userProfile.setId(1L);
        userProfile.setUserName(USERNAME);
        return userProfile;
    }
}
