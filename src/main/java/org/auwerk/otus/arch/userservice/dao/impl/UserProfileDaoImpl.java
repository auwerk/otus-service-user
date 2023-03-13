package org.auwerk.otus.arch.userservice.dao.impl;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.SqlResult;
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
    private static final String SQL_SELECT_BY_USERNAME = "SELECT * FROM user_profiles WHERE username=$1";
    private static final String SQL_UPDATE_BY_USERNAME = "UPDATE user_profiles "
            + "SET email=$1, first_name=$2, last_name=$3, birth_date=$4, phone_number=$5 "
            + "WHERE username=$6";

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
                        return mapRow(rows.iterator().next());
                    } else {
                        throw new UserProfileNotFoundException(id);
                    }
                }));
    }

    @Override
    public Uni<UserProfile> findByUserName(String userName) {
        return client.preparedQuery(SQL_SELECT_BY_USERNAME)
                .execute(Tuple.of(userName))
                .onItem().transform(Unchecked.function(rows -> {
                    if (rows.iterator().hasNext()) {
                        return mapRow(rows.iterator().next());
                    } else {
                        throw new UserProfileNotFoundException(userName);
                    }
                }));
    }

    @Override
    public Uni<Integer> updateByUserName(String userName, UserProfile profile) {
        return client.preparedQuery(SQL_UPDATE_BY_USERNAME)
                .execute(Tuple.of(profile.getEmail(), profile.getFirstName(), profile.getLastName(),
                        profile.getBirthDate(), profile.getPhoneNumber(), userName))
                .map(SqlResult::rowCount);
    }

    private static UserProfile mapRow(Row row) {
        final var userProfile = new UserProfile();
        userProfile.setId(row.getLong("id"));
        userProfile.setUserName(row.getString("username"));
        userProfile.setEmail(row.getString("email"));
        userProfile.setFirstName(row.getString("first_name"));
        userProfile.setLastName(row.getString("last_name"));
        userProfile.setBirthDate(row.getLocalDate("birth_date"));
        userProfile.setPhoneNumber(row.getInteger("phone_number"));
        return userProfile;
    }
}
