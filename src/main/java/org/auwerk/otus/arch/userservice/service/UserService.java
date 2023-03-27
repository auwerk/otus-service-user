package org.auwerk.otus.arch.userservice.service;

import io.smallrye.mutiny.Uni;
import org.auwerk.otus.arch.userservice.domain.UserProfile;

public interface UserService {
    
    Uni<Void> createUser(UserProfile profile, String initialPassword);

    Uni<UserProfile> getMyProfile();

    Uni<UserProfile> getUserProfile(Long id);

    Uni<Void> updateMyProfile(UserProfile profile);
}
