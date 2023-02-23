package org.auwerk.otus.arch.userservice.dao;

import io.smallrye.mutiny.Uni;
import org.auwerk.otus.arch.userservice.domain.UserProfile;

public interface UserProfileDao {
    Uni<Long> insert(UserProfile profile);

    Uni<UserProfile> findById(Long id);
}