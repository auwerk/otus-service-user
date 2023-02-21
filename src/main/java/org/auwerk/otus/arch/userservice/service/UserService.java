package org.auwerk.otus.arch.userservice.service;

import io.smallrye.mutiny.Uni;

public interface UserService {
    Uni<Long> createUser(String userName, String email);
}
