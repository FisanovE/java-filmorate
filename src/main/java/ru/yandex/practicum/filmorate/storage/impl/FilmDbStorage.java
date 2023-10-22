package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchParameter;
import ru.yandex.practicum.filmorate.model.SortParameter;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
    private static JdbcOperations jdbcOperations;

    @Autowired
    public FilmDbStorage(JdbcOperations jdbcOperations) {
        FilmDbStorage.jdbcOperations = jdbcOperations;
    }

    @Override
    public Film create(Film film) {
        String sqlRequest = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, " +
                "?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            return ps;
        }, keyHolder);

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(generatedId);

        return film;
    }

    @Override
    public void update(Film film) {
        String sqlRequest;
        if (film.getMpa() != null) {
            sqlRequest = String.format("UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                    "mpa_id = %d WHERE film_id = ?", film.getMpa().getId());
        } else {
            sqlRequest = String.format("UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                    "mpa_id = %s WHERE film_id = ?", "NULL");
        }

        jdbcOperations.update(sqlRequest, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getId());
        String sqlDeleteDirector = "DELETE FROM films_directors WHERE film_id = ?";
        jdbcOperations.update(sqlDeleteDirector, film.getId());
        String sqlDeleteGenre = "DELETE FROM films_genres WHERE film_id = ?";
        jdbcOperations.update(sqlDeleteGenre, film.getId());
    }

    @Override
    public Film getById(Long id) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa m ON m.mpa_id = f.mpa_id " +
                "WHERE f.film_id = ?";
        return jdbcOperations.queryForObject(sql, new FilmRowMapper(), id);
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa m ON m.mpa_id = f.mpa_id " +
                "ORDER BY film_id";
        return jdbcOperations.query(sql, new FilmRowMapper());
    }

    /**
     * ALG_6
     */
    @Override
    public void delete(Long filmId) {
        String sqlQuery = "DELETE FROM films WHERE FILM_ID = ?";
        jdbcOperations.update(sqlQuery, filmId);
    }

    @Override
    public void addLike(Long filmId, Long userId) { // проверка не перенесена в service для обход ошибки тестов
        SqlRowSet sqlRows = jdbcOperations.queryForRowSet("SELECT * FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
        if (!sqlRows.first()) {
            String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
            jdbcOperations.update(sql, filmId, userId);
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcOperations.update(sql, filmId, userId);
    }

    /**
     * ALG_8
     */
    @Override
    public Collection<Film> getTopRatingFilmsByGenreAndYear(int count, long genreId, int year) {
        log.debug("ALG_8.FilmDbStorage -> entered into DataBaseStorage");

        List<Film> films;
        StringJoiner joiner = new StringJoiner(" ");
        String sqlEnd = "GROUP BY F.FILM_ID ORDER BY COUNT(L.USER_ID) DESC " + "LIMIT ?;";
        joiner.add("SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, m.mpa_id, m.mpa_name " +
                "FROM FILMS AS F " +
                "LEFT JOIN mpa m ON m.mpa_id = f.mpa_id " +
                "LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID " +
                "LEFT JOIN FILMS_GENRES AS FG ON F.FILM_ID = FG.FILM_ID");
        if (genreId != -1 && year != -1) {
            joiner.add("WHERE YEAR(F.RELEASE_DATE) = ? AND FG.GENRE_ID = ?");
            String sql = joiner.add(sqlEnd).toString();
            log.info("вошли в поиск 2/2000");
            films = jdbcOperations.query(sql, new FilmRowMapper(), year, genreId, count);
        } else if (genreId != -1) {
            joiner.add("WHERE FG.GENRE_ID = ?");
            String sql = joiner.add(sqlEnd).toString();
            films = jdbcOperations.query(sql, new FilmRowMapper(), genreId, count);
        } else if (year != -1) {
            joiner.add("WHERE YEAR(F.RELEASE_DATE) = ?");
            String sql = joiner.add(sqlEnd).toString();
            films = jdbcOperations.query(sql, new FilmRowMapper(), year, count);
        } else {
            String sql = joiner.add(sqlEnd).toString();
            films = jdbcOperations.query(sql, new FilmRowMapper(), count);
        }

        return films;
    }

    /**
     * ALG_7
     */
    public Collection<Film> getAllFilmsByDirector(Long id, SortParameter sortBy) {
        String sql;
        if (sortBy == SortParameter.LIKES) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON m.mpa_id = f.mpa_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "INNER JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration " +
                    "ORDER BY COUNT(l.user_id) DESC";
        } else if (sortBy == SortParameter.YEAR) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON m.mpa_id = f.mpa_id " +
                    "JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date";
        } else {
            throw new NotFoundException("ALG_7. Invalid RequestParam:  " + sortBy);
        }
        return jdbcOperations.query(sql, new FilmRowMapper(), id);
    }

    /**
     * ALG_2
     */
    @Override
    public Collection<Film> searchFilms(String query, List<SearchParameter> by) {
        String sql;
        if (by.contains(SearchParameter.DIRECTOR) && !by.contains(SearchParameter.TITLE)) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON m.mpa_id = f.mpa_id " +
                    "JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "JOIN directors d ON fd.director_id = d.director_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "WHERE LOWER(d.director_name) LIKE LOWER('%" + query + "%') " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "ORDER BY COUNT(l.film_id)";
        } else if (!by.contains(SearchParameter.DIRECTOR) && by.contains(SearchParameter.TITLE)) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "FROM films AS f " +
                    "LEFT JOIN mpa m ON m.mpa_id = f.mpa_id " +
                    "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                    "WHERE LOWER(f.name) LIKE LOWER('%" + query + "%') " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "ORDER BY COUNT (l.film_id)";
        } else if (by.contains(SearchParameter.DIRECTOR) && by.contains(SearchParameter.TITLE)) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON m.mpa_id = f.mpa_id " +
                    "LEFT JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "WHERE LOWER (d.director_name) LIKE LOWER ('%" + query + "%') OR LOWER (f.name) LIKE LOWER ('%" + query + "%') " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "ORDER BY COUNT(l.film_id) DESC";
        } else {
            throw new NotFoundException("ALG_2. Invalid RequestParam:  " + by);
        }
        return jdbcOperations.query(sql, new FilmRowMapper());
    }

    /**
     * ALG_3
     */
    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        String sql = "SELECT F.FILM_ID, F.name, F.description, F.release_date, F.duration, m.mpa_id, m.mpa_name " +
                "FROM FILMS f " +
                "LEFT JOIN mpa m ON m.mpa_id = f.mpa_id " +
                "JOIN LIKES l ON F.FILM_ID = L.FILM_ID " +
                "WHERE F.FILM_ID IN (SELECT FILM_ID " +
                "FROM LIKES " +
                "WHERE FILM_ID IN (SELECT FILM_ID " +
                "FROM LIKES " +
                "WHERE USER_ID = ? ) " +
                "AND USER_ID = ? " +
                "GROUP BY FILM_ID) " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY COUNT(user_id) DESC";

        return jdbcOperations.query(sql, new FilmRowMapper(), userId, friendId);
    }

    @Override
    public Collection<Film> getFilmsRecommendationsForUser(Long id) {
        int filmsToRecommend = 15;
        String queryForUserLikes = "SELECT film_ID FROM likes WHERE user_ID = ?";

        String userLikesSet = String.format("(%s)", String.join(",",
                jdbcOperations.query(queryForUserLikes, (rs, rowNum) -> rs.getString("film_ID"), id)));

        String queryForFilms = "SELECT * FROM films AS f JOIN " +
                "(SELECT film_ID FROM likes WHERE user_ID IN " +
                "(SELECT user_ID FROM likes " +
                "WHERE film_ID IN " + userLikesSet + " AND user_ID <> ? " +
                "GROUP BY user_ID " +
                "ORDER BY COUNT(user_ID) DESC) AND film_ID NOT IN " + userLikesSet + " " +
                "LIMIT ?) fi ON f.film_ID = fi.film_ID " +
                "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id;";

        return jdbcOperations.query(queryForFilms, new FilmRowMapper(), id, filmsToRecommend);
    }

}
