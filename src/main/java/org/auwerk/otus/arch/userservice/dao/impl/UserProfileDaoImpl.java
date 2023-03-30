package org.auwerk.otus.arch.userservice.dao.impl;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import org.auwerk.otus.arch.userservice.dao.UserProfileDao;
import org.auwerk.otus.arch.userservice.domain.UserProfile;
import org.auwerk.otus.arch.userservice.exception.DaoException;

import java.util.NoSuchElementException;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserProfileDaoImpl implements UserProfileDao {

    @Override
    public Uni<Long> insert(PgPool pool, UserProfile profile) {
        return pool.preparedQuery(
                "INSERT INTO user_profiles (username, email, first_name, last_name, birth_date, phone_number) VALUES($1, $2, $3, $4, $5, $6) RETURNING id")
                .execute(Tuple.of(profile.getUserName(), profile.getEmail(), profile.getFirstName(),
                        profile.getLastName(), profile.getBirthDate(), profile.getPhoneNumber()))
                .map(rowSet -> {
                    final var rowSetIterator = rowSet.iterator();
                    if (!rowSetIterator.hasNext()) {
                        throw new DaoException("user profile insertion failed");
                    }
                    return rowSetIterator.next().getLong("id");
                });
    }

    @Override
    public Uni<UserProfile> findByUserName(PgPool pool, String userName) {
        return pool.preparedQuery("SELECT * FROM user_profiles WHERE username=$1")
                .execute(Tuple.of(userName))
                .map(rowSet -> {
                    final var rowSetIterator = rowSet.iterator();
                    if (!rowSetIterator.hasNext()) {
                        throw new NoSuchElementException("user profile not found, username=" + userName);
                    }
                    return mapRow(rowSetIterator.next());
                });
    }

    @Override
    public Uni<Void> updateById(PgPool pool, Long id, UserProfile profile) {
        return pool.preparedQuery(
                "UPDATE user_profiles SET email=$1, first_name=$2, last_name=$3, birth_date=$4, phone_number=$5 WHERE id=$6")
                .execute(Tuple.of(profile.getEmail(), profile.getFirstName(), profile.getLastName(),
                        profile.getBirthDate(), profile.getPhoneNumber(), id))
                .invoke(rowSet -> {
                    if (rowSet.rowCount() != 1) {
                        throw new DaoException("user profile update failed");
                    }
                })
                .replaceWithVoid();
    }

    @Override
    public Uni<Void> deleteById(PgPool pool, Long id) {
        return pool.preparedQuery("DELETE FROM user_profiles WHERE id=$1")
                .execute(Tuple.of(id))
                .invoke(rowSet -> {
                    if (rowSet.rowCount() != 1) {
                        throw new DaoException("user profile update failed");
                    }
                })
                .replaceWithVoid();
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
