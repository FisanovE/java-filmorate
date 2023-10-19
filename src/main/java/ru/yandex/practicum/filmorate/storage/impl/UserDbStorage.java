package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
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
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private DirectorDbStorage directorDbStorage;

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
        jdbcTemplate.update(sqlRequest, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM USERS ORDER BY USER_ID";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public User getUserById(Long id) {
        String sql = "select * from users where user_id = ?";
        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
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
    public Collection<User> getAllFriendsOfUser(Long idUser) {
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
     * ALG_6
     */
    //@Override
    public void deleteUser(Long id) {
        String sqlQuery = "DELETE FROM users WHERE USER_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    /**
     * ALG_4
     */
    @Override
    public List<Film> getFilmsRecommendationsForUser(Long id) {
        int filmsToRecommend = 15;
        String queryForUserLikes = "SELECT film_ID FROM likes WHERE user_ID = ?";

        String userLikesSet = String.format("(%s)", String.join(",",
                jdbcTemplate.query(queryForUserLikes, (rs, rowNum) -> rs.getString("film_ID"), id)));

        String queryForFilms = "SELECT * FROM films AS f JOIN " +
                "(SELECT film_ID FROM likes WHERE user_ID IN " +
                "(SELECT user_ID FROM likes " +
                "WHERE film_ID IN " + userLikesSet + " AND user_ID <> ? " +
                "GROUP BY user_ID " +
                "ORDER BY COUNT(user_ID) DESC) AND film_ID NOT IN " + userLikesSet + " " +
                "LIMIT ?) AS fi ON f.film_ID = fi.film_ID";

        List<Film> recommendations = jdbcTemplate.query(queryForFilms, new FilmRowMapper(), id, filmsToRecommend);

        for (Film film : recommendations) {
            film.setMpa(getMpaFromDataBase(film.getId()));
            film.setGenres(getGenresFromDataBase(film.getId()));
            film.setDirectors(getDirectorsFromDataBase(film.getId()));
        }
        return recommendations;
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
        SqlRowSet checkUser = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE user_id = ?", userId);
        if (!checkUser.next()) {
            log.info("ALG_5. Invalid User ID: {}", userId);
            throw new NotFoundException("ALG_5. Invalid User ID:  " + userId);
        }

        String sql = "SELECT * FROM events WHERE user_id = ?";

        log.info("ALG_5. Event was given for User with ID: " + userId);
        return jdbcTemplate.query(sql, new EventRowMapper(), userId);
    }

   /* @Override
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

        log.info("ALG_4. Films were recommended for User with ID: " + id);
        return recommendations;
    }*/

}
