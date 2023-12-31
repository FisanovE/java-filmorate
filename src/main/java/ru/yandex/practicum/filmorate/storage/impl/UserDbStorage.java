package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorage implements UserStorage {
    private final JdbcOperations jdbcOperations;

    @Override
    public User create(User user) {
        String sqlRequest = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcOperations.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(generatedId);

        log.info("User added: {} {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public void update(User user) {
        String sqlRequest = "UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE USER_ID = ?";
        int rows = jdbcOperations.update(sqlRequest, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (rows == 0) {
            throw new NotFoundException("Invalid User ID:  " + user.getId());
        }
    }

    /**
     * ALG_6
     */
    //@Override
    public void delete(Long id) {
        String sqlQuery = "DELETE FROM users WHERE USER_ID = ?";
        int rows = jdbcOperations.update(sqlQuery, id);
        if (rows == 0) {
            throw new NotFoundException("Invalid User ID: " + id);
        }
    }

    @Override
    public Collection<User> getAll() {
        String sql = "SELECT * FROM USERS ORDER BY USER_ID";
        return jdbcOperations.query(sql, new UserRowMapper());
    }

    public User getById(Long id) {
        try {
            String sql = "select * from users where user_id = ?";
            return jdbcOperations.queryForObject(sql, new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Invalid User ID: " + id);
        }
    }

    @Override
    public void addFriend(Long idUser, Long idFriend) {
        checkContainsUser(idFriend);
        String sqlRequest = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        int rows = jdbcOperations.update(sqlRequest, idUser, idFriend);
        if (rows == 0) {
            throw new NotFoundException("Invalid User ID " + idUser + " or Friend ID: " + idFriend);
        }
    }


    @Override
    public void deleteFriend(Long idUser, Long idFriend) {
        String sql = "DELETE FROM friends WHERE USER_ID = ? AND FRIEND_ID = ?";
        int rows = jdbcOperations.update(sql, idUser, idFriend);
        if (rows == 0) {
            throw new NotFoundException("Invalid User ID " + idUser + " or Friend ID: " + idFriend);
        }
    }

    @Override
    public Collection<User> getAllFriends(Long idUser) {
        checkContainsUser(idUser);
        String sql = "SELECT * FROM users WHERE user_id IN (select friend_id FROM friends WHERE user_id = ?) " +
                "ORDER BY user_id";
        return jdbcOperations.query(sql, new UserRowMapper(), idUser);
    }

    @Override
    public Collection<User> getMutualFriends(Long idUser, Long idOtherUser) {
        checkContainsUser(idUser);
        checkContainsUser(idOtherUser);
        String sql = "SELECT * FROM USERS WHERE user_ID IN (SELECT friend_id FROM (SELECT friend_id FROM friends " +
                "WHERE user_id IN (?, ?) AND friend_id NOT IN (?, ?) " +
                "ORDER BY friend_id) AS ids " +
                "GROUP BY " + "friend_id HAVING count(friend_id)>1) " +
                "ORDER BY USER_ID";
        return jdbcOperations.query(sql, new UserRowMapper(), idUser, idOtherUser, idUser, idOtherUser);
    }

    public void checkContainsUser(Long id) {
        SqlRowSet sqlRows = jdbcOperations.queryForRowSet("SELECT * FROM users WHERE user_id = ?", id);
        if (sqlRows.first()) {
            log.info("User found: {}", id);
        } else {
            log.info("User not found: {}", id);
            throw new NotFoundException("User not found: " + id);
        }
    }

}
