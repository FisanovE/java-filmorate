package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import static ru.yandex.practicum.filmorate.storage.impl.FilmDbStorage.*;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final DirectorDbStorage directorDbStorage;

    @Override
    public User create(User user) {
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
    public void update(User user) {
        String sqlRequest = "UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE USER_ID = ?";
        jdbcTemplate.update(sqlRequest, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
    }

    @Override
    public Collection<User> getAll() {
        String sql = "SELECT * FROM USERS ORDER BY USER_ID";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public User getById(Long id) {
        String sql = "select * from users where user_id = ?";
        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
    }

    /**
     * ALG_6
     */
    //@Override
    public void delete(Long id) {
        String sqlQuery = "DELETE FROM users WHERE USER_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public void addFriend(Long idUser, Long idFriend) {
        String sqlRequest = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlRequest, idUser, idFriend);
        addEvent(idUser, "FRIEND", "ADD", idFriend);
    }


    @Override
    public void deleteFriend(Long idUser, Long idFriend) {
        String sql = "DELETE FROM friends WHERE USER_ID = ? AND FRIEND_ID = ?";
        jdbcTemplate.update(sql, idUser, idFriend);
        addEvent(idUser, "FRIEND", "REMOVE", idFriend);
    }

    @Override
    public Collection<User> getAllFriends(Long idUser) {
        String sql = "SELECT * FROM users WHERE user_id IN (select friend_id FROM friends WHERE user_id = ?) " +
                "ORDER BY user_id";
        return jdbcTemplate.query(sql, new UserRowMapper(), idUser);
    }

    @Override
    public Collection<User> getMutualFriends(Long idUser, Long idOtherUser) {
        String sql = "SELECT * FROM USERS WHERE user_ID IN (SELECT friend_id FROM (SELECT friend_id FROM friends " +
                "WHERE user_id IN (?, ?) AND friend_id NOT IN (?, ?) " +
                "ORDER BY friend_id) AS ids " +
                "GROUP BY " + "friend_id HAVING count(friend_id)>1) " +
                "ORDER BY USER_ID";
        return jdbcTemplate.query(sql, new UserRowMapper(), idUser, idOtherUser, idUser, idOtherUser);
    }

    /**
     * ALG5
     */
    private void addEvent(Long userId, String eventType, String operation, Long entityId) {
        String sql = "INSERT INTO events (user_id, event_type, operation, entity_id, time_stamp) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, eventType, operation, entityId, System.currentTimeMillis());
    }

    /**
     * ALG5
     */
    @Override
    public List<Event> getEvents(Long userId) {
        String sql = "SELECT * FROM events WHERE user_id = ?";
        return jdbcTemplate.query(sql, new EventRowMapper(), userId);
    }

    @Override
    public List<Film> getFilmsRecommendationsForUser(Long id) {
        int filmsToRecommend = 15;
        String queryForUserLikes = "SELECT film_ID FROM likes WHERE user_ID = ?";

        String userLikesSet = String.format("(%s)", String.join(",",
                jdbcTemplate.query(queryForUserLikes, (rs, rowNum) -> rs.getString("film_ID"), id)));

        String queryForFilms = "SELECT * FROM films AS f RIGHT JOIN " +
                "(SELECT film_ID FROM likes WHERE user_ID IN " +
                "(SELECT user_ID FROM likes " +
                "WHERE film_ID IN " + userLikesSet + " AND user_ID <> ? " +
                "GROUP BY user_ID " +
                "ORDER BY COUNT(user_ID) DESC) AND film_ID NOT IN " + userLikesSet + " " +
                "LIMIT ?) fi ON f.film_ID = fi.film_ID " +
                "LEFT JOIN films_mpa fm ON f.film_id = fm.film_id " +
                "LEFT JOIN mpa m ON fm.mpa_id = m.mpa_id;";

        List<Film> recommendations = jdbcTemplate.query(queryForFilms, new FilmRowMapperNew(), id, filmsToRecommend);

        loadGenres(recommendations);
        directorDbStorage.loadDirectors(recommendations);

        return recommendations;
    }

}
