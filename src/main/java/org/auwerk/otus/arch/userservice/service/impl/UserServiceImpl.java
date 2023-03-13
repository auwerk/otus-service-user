package org.auwerk.otus.arch.userservice.service.impl;

import io.quarkus.runtime.util.StringUtil;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.auwerk.otus.arch.userservice.dao.UserProfileDao;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.mapper.UserProfileMapper;
import org.auwerk.otus.arch.userservice.service.UserService;
import org.auwerk.otus.arch.userservice.service.exception.KeycloakIntegrationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserServiceImpl implements UserService {

    private final Vertx vertx;
    private final Keycloak keycloak;
    private final SecurityIdentity securityIdentity;
    private final UserProfileDao userProfileDao;
    private final UserProfileMapper userProfileMapper;
    private final String keycloakRealm;

    public UserServiceImpl(Vertx vertx, Keycloak keycloak, SecurityIdentity securityIdentity,
                           UserProfileDao userProfileDao, UserProfileMapper userProfileMapper,
                           @ConfigProperty(name = "otus.service.user.keycloak-realm") String keycloakRealm) {
        this.vertx = vertx;
        this.keycloak = keycloak;
        this.securityIdentity = securityIdentity;
        this.userProfileDao = userProfileDao;
        this.userProfileMapper = userProfileMapper;
        this.keycloakRealm = keycloakRealm;
    }

    @Override
    public Uni<Long> createUser(UserProfile profile, String initialPassword) {
        return createUserInKeycloak(profile, initialPassword)
                .onItem().transformToUni(userProfileDao::insert);
    }

    @Override
    public Uni<UserProfile> getMyProfile() {
        return userProfileDao.findByUserName(securityIdentity.getPrincipal().getName());
    }

    @Override
    public Uni<Integer> updateMyProfile(UserProfile profile) {
        return userProfileDao.updateByUserName(securityIdentity.getPrincipal().getName(), profile);
    }

    private Uni<UserProfile> createUserInKeycloak(UserProfile profile, String initialPassword) {
        return vertx.getOrCreateContext().executeBlocking(
                Uni.createFrom().emitter(emitter -> {
                    final var userRepresentation = new UserRepresentation();
                    userRepresentation.setEnabled(true);
                    userRepresentation.setEmailVerified(true);

                    userProfileMapper.updateUserRepresentationFromProfile(profile, userRepresentation);

                    final var response = keycloak.realm(keycloakRealm).users()
                            .create(userRepresentation);

                    if (response.getStatus() == 201) {
                        if (!StringUtil.isNullOrEmpty(initialPassword)) {
                            resetUserPasswordInKeycloak(profile.getUserName(), initialPassword);
                        }
                        emitter.complete(profile);
                    } else {
                        emitter.fail(new KeycloakIntegrationException(response.getStatus()));
                    }
                }));
    }

    private void resetUserPasswordInKeycloak(String userName, String password) {
        final var realm = keycloak.realm(keycloakRealm);

        final var userProfiles = realm.users()
                .searchByUsername(userName, true);
        if (!userProfiles.isEmpty()) {
            final var passwordCredential = new CredentialRepresentation();
            passwordCredential.setId(CredentialRepresentation.PASSWORD);
            passwordCredential.setValue(password);

            realm.users().get(userProfiles.get(0).getId()).resetPassword(passwordCredential);
        }
    }
}
