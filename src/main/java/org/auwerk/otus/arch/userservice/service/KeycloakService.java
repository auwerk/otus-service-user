package org.auwerk.otus.arch.userservice.service;

import org.auwerk.otus.arch.userservice.domain.UserProfile;

import io.smallrye.mutiny.Uni;

public interface KeycloakService {
    
    Uni<Void> createUserAccount(UserProfile profile);

    Uni<Void> deleteUserAccount(UserProfile profile);

    Uni<Void> setUserPassword(UserProfile profile, String password);
}
