package org.auwerk.otus.arch.userservice.dao;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

import org.auwerk.otus.arch.userservice.domain.UserProfile;

public interface UserProfileDao {

    Uni<Long> insert(PgPool pool, UserProfile profile);

    Uni<UserProfile> findByUserName(PgPool pool, String userName);

    Uni<Void> updateById(PgPool pool, Long id, UserProfile profile);

    Uni<Void> deleteById(PgPool pool, Long id);
}
