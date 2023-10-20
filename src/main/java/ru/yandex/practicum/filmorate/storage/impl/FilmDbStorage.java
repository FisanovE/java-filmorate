package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {
    private static JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        FilmDbStorage.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        String sqlRequest = "INSERT INTO films (name, description, release_date, duration) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            return ps;
        }, keyHolder);

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(generatedId);

        setMpaInDataBase(film);
        setGenreInDataBase(film);
        setDirectorInDataBase(film);

        return film;
    }

    @Override
    public void update(Film film) {
        String sqlRequest = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ? " + "WHERE film_id = ?";
        jdbcTemplate.update(sqlRequest, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getId());
        setMpaInDataBase(film);
        setGenreInDataBase(film);
        film.setGenres(getGenresFromDataBase(film.getId()));
        setDirectorInDataBase(film);
    }

    @Override
    public Film getById(Long id) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                "FROM films AS f " +
                "LEFT JOIN films_mpa fm ON fm.film_id = f.film_id " +
                "LEFT JOIN mpa m ON m.mpa_id = fm.mpa_id " +
                "WHERE f.film_id = ?";
        Film film = jdbcTemplate.queryForObject(sql, new FilmRowMapper(), id);
        setInFilmGenresAndDirectors(film);
        return film;
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                "FROM films AS f " +
                "LEFT JOIN films_mpa fm ON fm.film_id = f.film_id " +
                "LEFT JOIN mpa m ON m.mpa_id = fm.mpa_id " +
                "ORDER BY film_id";
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());
        for (Film film : films) {
            setInFilmGenresAndDirectors(film);
        }
        return films;
    }

    /**
     * ALG_6
     */
    @Override
    public void delete(Long filmId) {
        String sqlQuery = "DELETE FROM films WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    private void setMpaInDataBase(Film film) {
        String sqlDeleteMpa = "DELETE FROM films_mpa WHERE film_id = ?";
        jdbcTemplate.update(sqlDeleteMpa, film.getId());

        if (film.getMpa() != null) {
            String sqlRatings = "INSERT INTO films_mpa (film_id, mpa_id) VALUES (?, ?)";
            jdbcTemplate.update(sqlRatings, film.getId(), film.getMpa().getId());
        }
    }

    private void setGenreInDataBase(Film film) {
        String sqlDeleteGenre = "DELETE FROM films_genres WHERE film_id = ?";
        jdbcTemplate.update(sqlDeleteGenre, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            LinkedHashSet<Genre> set = film.getGenres();
            film.setGenres(set);
            for (Genre genre : set) {
                String sqlGenres = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";
                jdbcTemplate.update(sqlGenres, film.getId(), genre.getId());
            }
        }
    }

    private void setInFilmGenresAndDirectors(Film film) {
        String sqlGenreAndDirector = "(SELECT fg.film_id, fg.genre_id, g.genre_name, fd.director_id, d.director_name " +
                "FROM films_genres AS fg " +
                "LEFT JOIN films_directors AS fd ON fg.film_id = fd.film_id " +
                "LEFT JOIN directors AS d ON d.director_id = fd.director_id " +
                "LEFT JOIN genres AS g ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? ) " +
                "UNION ALL " +
                "(SELECT fd.film_id, fg.genre_id, g.genre_name, fd.director_id, d.director_name " +
                "FROM films_directors AS fd " +
                "LEFT JOIN films_genres AS fg ON fg.film_id = fd.film_id " +
                "LEFT JOIN directors AS d ON d.director_id = fd.director_id " +
                "LEFT JOIN genres AS g ON g.genre_id = fg.genre_id " +
                "WHERE fd.film_id = ? )";

        SqlRowSet unionRows = jdbcTemplate.queryForRowSet(sqlGenreAndDirector, film.getId(), film.getId());
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        LinkedHashSet<Director> directors = new LinkedHashSet<>();
        while (unionRows.next()) {

            if (unionRows.getString("genre_id") != null) {
                Genre genre = Genre.builder()
                        .id(Long.valueOf(Objects.requireNonNull(unionRows.getString("genre_id"))))
                        .name(unionRows.getString("genre_name"))
                        .build();
                genres.add(genre);
            }

            if (unionRows.getString("director_id") != null) {
                Director director = Director.builder()
                        .id(Long.valueOf(Objects.requireNonNull(unionRows.getString("director_id"))))
                        .name(unionRows.getString("director_name"))
                        .build();
                directors.add(director);
            }
        }
        film.setGenres(genres);
        film.setDirectors(directors);
    }

    static LinkedHashSet<Genre> getGenresFromDataBase(Long id) {
        String sql = "SELECT genre_id, genre_name FROM genres WHERE genre_id IN (SELECT genre_id " + "FROM films_genres WHERE film_id = ?)";
        return new LinkedHashSet<>(jdbcTemplate.query(sql, new GenreRowMapper(), id));
    }

    @Override
    public void addLike(Long filmId, Long userId) { // проверка не перенесена в service для обход ошибки тестов
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("SELECT * FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
        if (!sqlRows.first()) {
            String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, filmId, userId);
        }
        addEvent(userId, "LIKE", "ADD", filmId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        addEvent(userId, "LIKE", "REMOVE", filmId);
    }

    @Override
    public Collection<Film> getTopRatingFilms(int count) {
        String sql = "SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, m.mpa_id, m.mpa_name " +
                "FROM FILMS AS F " +
                "LEFT JOIN films_mpa fm ON fm.film_id = f.film_id " +
                "LEFT JOIN mpa m ON m.mpa_id = fm.mpa_id " +
                "LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY COUNT(L.USER_ID) DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), count);

        for (Film film : films) {
            setInFilmGenresAndDirectors(film);
        }
        return films;
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
                "LEFT JOIN films_mpa fm ON fm.film_id = f.film_id " +
                "LEFT JOIN mpa m ON m.mpa_id = fm.mpa_id " +
                "LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID " +
                "LEFT JOIN FILMS_GENRES AS FG ON F.FILM_ID = FG.FILM_ID");
        if (genreId != -1 && year != -1) {
            joiner.add("WHERE YEAR(F.RELEASE_DATE) = ? AND FG.GENRE_ID = ?");
            String sql = joiner.add(sqlEnd).toString();
            log.info("вошли в поиск 2/2000");
            films = jdbcTemplate.query(sql, new FilmRowMapper(), year, genreId, count);
        } else if (genreId != -1) {
            joiner.add("WHERE FG.GENRE_ID = ?");
            String sql = joiner.add(sqlEnd).toString();
            films = jdbcTemplate.query(sql, new FilmRowMapper(), genreId, count);
        } else {
            joiner.add("WHERE YEAR(F.RELEASE_DATE) = ?");
            String sql = joiner.add(sqlEnd).toString();
            films = jdbcTemplate.query(sql, new FilmRowMapper(), year, count);
        }

        for (Film film : films) {
            setInFilmGenresAndDirectors(film);
        }
        return films;
    }

    @Override
    public Collection<Genre> getAllGenres() {
        String sql = "SELECT genre_id, genre_name FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sql, new GenreRowMapper());
    }

    @Override
    public Genre getGenresById(Long id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        return jdbcTemplate.queryForObject(sql, new GenreRowMapper(), id);
    }

    @Override
    public Collection<Mpa> getAllRatingsMpa() {
        String sql = "SELECT mpa_id, mpa_name FROM mpa ORDER BY mpa_id";
        return jdbcTemplate.query(sql, new MpaRowMapper());
    }

    @Override
    public Mpa getRatingsMpaById(Long id) {
        String sql = "SELECT * FROM mpa WHERE mpa_id = ?";
        return jdbcTemplate.queryForObject(sql, new MpaRowMapper(), id);
    }

    private void setDirectorInDataBase(Film film) {
        String sqlDeleteDirector = "DELETE FROM films_directors WHERE film_id = ?";
        jdbcTemplate.update(sqlDeleteDirector, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            LinkedHashSet<Director> set = film.getDirectors();
            film.setDirectors(set);
            for (Director director : set) {
                String sqlDirectors = "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)";
                jdbcTemplate.update(sqlDirectors, film.getId(), director.getId());
            }
        }
    }

    /**
     * ALG_7
     */
    public Collection<Film> getAllFilmsByDirector(Long id, String sortBy) {
        String sql;
        if (Objects.equals(sortBy, "likes")) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN films_mpa fm ON fm.film_id = f.film_id " +
                    "LEFT JOIN mpa m ON m.mpa_id = fm.mpa_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "INNER JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration " +
                    "ORDER BY COUNT(l.user_id) DESC";
        } else if (Objects.equals(sortBy, "year")) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN films_mpa fm ON fm.film_id = f.film_id " +
                    "LEFT JOIN mpa m ON m.mpa_id = fm.mpa_id " +
                    "JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date";
        } else {
            throw new NotFoundException("ALG_7. Invalid RequestParam:  " + sortBy);
        }
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), id);
        for (Film film : films) {
            setInFilmGenresAndDirectors(film);
        }
        return films;
    }

    /**
     * ALG_2
     */
    @Override
    public Collection<Film> searchFilms(String query, String by) {
        String sql;
        if (Objects.equals(by, "director")) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN films_mpa fm ON fm.film_id = f.film_id " +
                    "LEFT JOIN mpa m ON m.mpa_id = fm.mpa_id " +
                    "JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "JOIN directors d ON fd.director_id = d.director_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "WHERE LOWER(d.director_name) LIKE LOWER('%" + query + "%') " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "ORDER BY COUNT(l.film_id)";
        } else if (Objects.equals(by, "title")) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "FROM films AS f " +
                    "LEFT JOIN films_mpa fm ON fm.film_id = f.film_id " +
                    "LEFT JOIN mpa m ON m.mpa_id = fm.mpa_id " +
                    "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                    "WHERE LOWER(f.name) LIKE LOWER('%" + query + "%') " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "ORDER BY COUNT (l.film_id)";
        } else if (Objects.equals(by, "title,director") || Objects.equals(by, "director,title")) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN films_mpa fm ON fm.film_id = f.film_id " +
                    "LEFT JOIN mpa m ON m.mpa_id = fm.mpa_id " +
                    "LEFT JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "WHERE LOWER (d.director_name) LIKE LOWER ('%" + query + "%') OR LOWER (f.name) LIKE LOWER ('%" + query + "%') " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, m.mpa_id, m.mpa_name " +
                    "ORDER BY COUNT(l.film_id) DESC";
        } else {
            throw new NotFoundException("ALG_2. Invalid RequestParam:  " + by);
        }
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());
        for (Film film : films) {
            setInFilmGenresAndDirectors(film);
        }
        return films;
    }

    /**
     * ALG_3
     */
    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        String sql = "SELECT F.FILM_ID, F.name, F.description, F.release_date, F.duration, m.mpa_id, m.mpa_name " +
                "FROM FILMS f " +
                "LEFT JOIN films_mpa fm ON fm.film_id = f.film_id " +
                "LEFT JOIN mpa m ON m.mpa_id = fm.mpa_id " +
                "LEFT JOIN LIKES l ON F.FILM_ID = L.FILM_ID " +
                "WHERE F.FILM_ID IN (SELECT FILM_ID " +
                "FROM LIKES " +
                "WHERE FILM_ID IN (SELECT FILM_ID " +
                "FROM LIKES " +
                "WHERE USER_ID = ? ) " +
                "AND USER_ID = ? " +
                "GROUP BY FILM_ID) " +
                "GROUP BY F.FILM_ID " +
                "ORDER BY COUNT(user_id) DESC";

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), userId, friendId);

        for (Film film : films) {
            setInFilmGenresAndDirectors(film);
        }
        return films;
    }

    /**
     * ALG5
     */
    public void addEvent(Long userId, String eventType, String operation, Long entityId) {
        String sql = "INSERT INTO events (user_id, event_type, operation, entity_id, time_stamp) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, eventType, operation, entityId, System.currentTimeMillis());
    }

    public static void loadGenres(List<Film> films) {
        final Map<Long, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, identity()));
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sqlQuery = "select * from GENRES g, films_genres fg " +
                "where fg.GENRE_ID = g.GENRE_ID AND fg.FILM_ID in (" + inSql + ")";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            final Film film = filmById.get(rs.getLong("FILM_ID"));
            film.getGenres().add(new GenreRowMapper().mapRow(rs, 0));
        }, films.stream().map(Film::getId).toArray());
    }

}
