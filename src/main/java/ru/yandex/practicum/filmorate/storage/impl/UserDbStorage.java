package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addNewUser(User user) {
        String sqlRequest = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
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
    public User updateUser(User user) {
        String sqlRequest = "UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE USER_ID = ?";
        int rowsUpdated = jdbcTemplate.update(sqlRequest, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Invalid User ID:  " + user.getId());
        }

        log.info("User update: {} {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM USERS ORDER BY USER_ID";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public User getUserById(Long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users where user_id = ?", id);
        if (userRows.next()) {
            log.info("User found: {} {}", userRows.getString("user_id"), userRows.getString("name"));

            return User.builder().id(userRows.getLong("user_id")).email(userRows.getString("email"))
                    .login(userRows.getString("login")).name(userRows.getString("name"))
                    .birthday(Objects.requireNonNull(userRows.getDate("birthday")).toLocalDate()).build();
        } else {
            log.info("Invalid User ID: {}", id);
            throw new NotFoundException("Invalid User ID:  " + id);
        }
    }

    @Override
    public void addFriend(Long idUser, Long idFriend) {
        if (idFriend <= 0) {
            throw new NotFoundException("Invalid User ID:  " + idFriend);
        }

        String sqlRequest = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        int rowsUpdated = jdbcTemplate.update(sqlRequest, idUser, idFriend);

        if (rowsUpdated == 0) {
            throw new NotFoundException("User ID is missing in friends:  " + idFriend);
        }
        log.info("Added friends ID: {}", idFriend);
    }


    @Override
    public void deleteFriend(Long idUser, Long idFriend) {
        String sql = "DELETE FROM friends WHERE USER_ID = ? AND FRIEND_ID = ?";
        jdbcTemplate.update(sql, idUser, idFriend);
    }

    @Override
    public Collection<User> getAllFriendsOfUser(Long idUser) {
        String sql = "SELECT * FROM USERS WHERE user_ID IN (select friend_id FROM friends WHERE user_id = ?) " + "ORDER BY USER_ID";

        return jdbcTemplate.query(sql, new UserRowMapper(), idUser);
    }

    @Override
    public Collection<User> getMutualFriends(Long idUser, Long idOtherUser) {
        String sql = "SELECT * FROM USERS WHERE user_ID IN (SELECT friend_id FROM (SELECT friend_id FROM friends " + "WHERE user_id IN (?, ?) AND friend_id NOT IN (?, ?) ORDER BY friend_id) AS ids GROUP BY " + "friend_id HAVING count(friend_id)>1) ORDER BY USER_ID";

        return jdbcTemplate.query(sql, new UserRowMapper(), idUser, idOtherUser, idUser, idOtherUser);
    }

    @Override
    public void deleteUser(Long id) {
        String sqlQuery = "DELETE FROM users WHERE USER_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }
}
