package org.auwerk.otus.arch.userservice.dao.impl;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import org.auwerk.otus.arch.userservice.dao.UserProfileDao;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.service.exception.UserProfileNotFoundException;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RequiredArgsConstructor
public class UserProfileDaoImpl implements UserProfileDao {

    private static final String SQL_INSERT = "INSERT INTO user_profiles "
            + "(username, email, first_name, last_name, birth_date, phone_number) "
            + "VALUES($1, $2, $3, $4, $5, $6) RETURNING id";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM user_profiles WHERE id=$1";

    private final PgPool client;

    @Override
    public Uni<Long> insert(UserProfile profile) {
        return client.preparedQuery(SQL_INSERT)
                .execute(Tuple.of(profile.getUserName(), profile.getEmail(), profile.getFirstName(),
                        profile.getLastName(), profile.getBirthDate(), profile.getPhoneNumber()))
                .onItem().transform(rows -> rows.iterator().next().getLong("id"));
    }

    @Override
    public Uni<UserProfile> findById(Long id) {
        return client.preparedQuery(SQL_SELECT_BY_ID)
                .execute(Tuple.of(id))
                .onItem().transform(Unchecked.function(rows -> {
                    if (rows.iterator().hasNext()) {
                        final var row = rows.iterator().next();
                        final var userProfile = new UserProfile();
                        userProfile.setId(row.getLong("id"));
                        userProfile.setUserName(row.getString("username"));
                        userProfile.setEmail(row.getString("email"));
                        userProfile.setFirstName(row.getString("first_name"));
                        userProfile.setLastName(row.getString("last_name"));
                        userProfile.setBirthDate(row.getLocalDate("birth_date"));
                        userProfile.setPhoneNumber(row.getInteger("phone_number"));
                        return userProfile;
                    } else {
                        throw new UserProfileNotFoundException(id);
                    }
                }));
    }
}