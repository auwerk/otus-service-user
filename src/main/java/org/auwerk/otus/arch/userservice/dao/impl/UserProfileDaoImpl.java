package org.auwerk.otus.arch.userservice.dao.impl;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import org.auwerk.otus.arch.userservice.dao.UserProfileDao;
import org.auwerk.otus.arch.userservice.domain.UserProfile;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RequiredArgsConstructor
public class UserProfileDaoImpl implements UserProfileDao {

    private static final String SQL_INSERT = "INSERT INTO user_profiles (username, email, first_name, last_name, birth_date) VALUES($1, $2, $3, $4, $5) RETURNING id";

    private final PgPool client;

    @Override
    public Uni<Long> insert(UserProfile profile) {
        return client.preparedQuery(SQL_INSERT)
                .execute(Tuple.of(profile.getUserName(), profile.getEmail(), profile.getFirstName(),
                        profile.getLastName(), profile.getBirthDate()))
                .onItem().transform(rows -> rows.iterator().next().getLong("id"));
    }
}
