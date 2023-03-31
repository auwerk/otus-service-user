package org.auwerk.otus.arch.userservice.service;

import io.smallrye.mutiny.Uni;
import org.auwerk.otus.arch.userservice.domain.UserProfile;

public interface UserProfileService {
    
    Uni<Long> createUserProfile(UserProfile profile, String initialPassword);

    Uni<UserProfile> getMyProfile();

    Uni<Void> updateMyProfile(UserProfile profile);

    Uni<Void> deleteMyProfile();
}
