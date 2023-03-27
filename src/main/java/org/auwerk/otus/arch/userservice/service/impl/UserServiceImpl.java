package org.auwerk.otus.arch.userservice.service.impl;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import lombok.RequiredArgsConstructor;

import org.auwerk.otus.arch.userservice.dao.UserProfileDao;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.exception.UserProfileNotFoundException;
import org.auwerk.otus.arch.userservice.service.KeycloakService;
import org.auwerk.otus.arch.userservice.service.UserService;

import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PgPool pool;
    private final SecurityIdentity securityIdentity;
    private final UserProfileDao userProfileDao;
    private final KeycloakService keycloakService;

    @Override
    public Uni<Void> createUser(UserProfile profile, String initialPassword) {
        return keycloakService.createUser(profile)
                .chain(() -> keycloakService.setUserPassword(profile, initialPassword));
    }

    @Override
    public Uni<UserProfile> getMyProfile() {
        return userProfileDao.findByUserName(pool, getUserName())
                .onFailure(NoSuchElementException.class)
                .transform(f -> new UserProfileNotFoundException(getUserName()));
    }

    @Override
    public Uni<UserProfile> getUserProfile(Long id) {
        return userProfileDao.findById(pool, id)
                .onFailure(NoSuchElementException.class)
                .transform(f -> new UserProfileNotFoundException(id));
    }

    @Override
    public Uni<Void> updateMyProfile(UserProfile profile) {
        return userProfileDao.updateByUserName(pool, getUserName(), profile);
    }

    private String getUserName() {
        return securityIdentity.getPrincipal().getName();
    }
}
