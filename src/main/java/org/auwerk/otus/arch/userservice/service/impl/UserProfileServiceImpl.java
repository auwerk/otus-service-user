package org.auwerk.otus.arch.userservice.service.impl;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import lombok.RequiredArgsConstructor;

import org.auwerk.otus.arch.userservice.dao.UserProfileDao;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.exception.UserProfileNotFoundException;
import org.auwerk.otus.arch.userservice.service.BillingService;
import org.auwerk.otus.arch.userservice.service.KeycloakService;
import org.auwerk.otus.arch.userservice.service.UserProfileService;

import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final PgPool pool;
    private final SecurityIdentity securityIdentity;
    private final UserProfileDao userProfileDao;
    private final KeycloakService keycloakService;
    private final BillingService billingService;

    @Override
    public Uni<Long> createUserProfile(UserProfile profile, String initialPassword) {
        return keycloakService.createUserAccount(profile)
                .chain(() -> keycloakService.setUserPassword(profile, initialPassword))
                .chain(() -> billingService.createUserAccount(profile.getUserName()))
                .chain(() -> userProfileDao.insert(pool, profile));
    }

    @Override
    public Uni<UserProfile> getMyProfile() {
        return userProfileDao.findByUserName(pool, getUserName())
                .onFailure(NoSuchElementException.class)
                .transform(f -> new UserProfileNotFoundException(getUserName()));
    }

    @Override
    public Uni<Void> updateMyProfile(UserProfile profile) {
        return pool.withTransaction(conn -> userProfileDao.findByUserName(pool, getUserName())
                .flatMap(p -> userProfileDao.updateById(pool, p.getId(), profile)))
                .onFailure(NoSuchElementException.class)
                .transform(f -> new UserProfileNotFoundException(getUserName()));
    }

    @Override
    public Uni<Void> deleteMyProfile() {
        return userProfileDao.findByUserName(pool, getUserName())
                .flatMap(userProfile -> Uni.combine().all().unis(keycloakService.deleteUserAccount(userProfile),
                        billingService.deleteUserAccount(getUserName()),
                        userProfileDao.deleteById(pool, userProfile.getId())).discardItems())
                .onFailure(NoSuchElementException.class)
                .transform(f -> new UserProfileNotFoundException(getUserName()));
    }

    private String getUserName() {
        return securityIdentity.getPrincipal().getName();
    }
}
