package org.auwerk.otus.arch.userservice.service;

import io.smallrye.mutiny.Uni;
import org.auwerk.otus.arch.userservice.domain.UserProfile;

public interface UserService {
    Uni<Long> createUser(UserProfile profile);
}