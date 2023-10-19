package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
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
    private final ReviewRowMapper reviewRowMapper = new ReviewRowMapper();

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        FilmDbStorage.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addNewFilm(Film film) {
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
    public Film updateFilm(Film film) {
        String sqlRequest = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ? " + "WHERE film_id = ?";
        jdbcTemplate.update(sqlRequest, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getId());
        setMpaInDataBase(film);
        setGenreInDataBase(film);
        film.setGenres(getGenresFromDataBase(film.getId()));
        setDirectorInDataBase(film);
        return film;
    }

    @Override
    public Film getFilmById(Long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        Film film = jdbcTemplate.queryForObject(sql, new FilmRowMapper(), id);
        film.setMpa(getMpaFromDataBase(id));
        film.setGenres(getGenresFromDataBase(id));
        film.setDirectors(getDirectorsFromDataBase(id));
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT * FROM films ORDER BY film_id";
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());
        for (Film film : films) {
            film.setMpa(getMpaFromDataBase(film.getId()));
            film.setGenres(getGenresFromDataBase(film.getId()));
            film.setDirectors(getDirectorsFromDataBase(film.getId()));
        }
        return films;
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

    static Mpa getMpaFromDataBase(Long id) {
        Mpa mpa = null;
        String sqlMpa = "SELECT mpa_id, mpa_name FROM mpa WHERE mpa_id IN (SELECT mpa_id FROM films_mpa " + "WHERE film_id = ?)";
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(sqlMpa, id);
        while (mpaRows.next()) {
            mpa = Mpa.builder().id(mpaRows.getLong("mpa_id")).name(mpaRows.getString("mpa_name")).build();
        }
        return mpa;
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
        String sql = "SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION FROM FILMS AS F " +
                "LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID GROUP BY F.FILM_ID ORDER BY COUNT(L.USER_ID) DESC " +
                "LIMIT ?;";

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), count);

        for (Film film : films) {
            film.setMpa(getMpaFromDataBase(film.getId()));
            film.setGenres(getGenresFromDataBase(film.getId()));
            film.setDirectors(getDirectorsFromDataBase(film.getId()));
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
        joiner.add("SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION FROM FILMS AS F " + "LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID " + "LEFT JOIN FILMS_GENRES AS FG ON F.FILM_ID = FG.FILM_ID");
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
            film.setMpa(getMpaFromDataBase(film.getId()));
            film.setGenres(getGenresFromDataBase(film.getId()));
            film.setDirectors(getDirectorsFromDataBase(film.getId()));
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

    /**
     * ALG_6
     */
    @Override
    public void deleteFilm(Long filmId) {
        String sqlQuery = "DELETE FROM films WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId);
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

    static LinkedHashSet<Director> getDirectorsFromDataBase(Long id) {
        String sql = "SELECT * FROM directors WHERE director_id IN (SELECT director_id FROM films_directors WHERE " +
                "film_id = ?)";
        return new LinkedHashSet<>(jdbcTemplate.query(sql, new DirectorRowMapper(), id));
    }

    /**
     * ALG_7
     */
    public Collection<Film> getAllFilmsByDirector(Long id, String sortBy) {
        String sql;
        if (Objects.equals(sortBy, "likes")) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, COUNT(l.user_id) " +
                    "FROM films f LEFT JOIN likes l ON f.film_id = l.film_id INNER JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration " +
                    "ORDER BY COUNT(l.user_id) DESC";
        } else if (Objects.equals(sortBy, "year")) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration " +
                    "FROM films f JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date";
        } else {
            throw new NotFoundException("ALG_7. Invalid RequestParam:  " + sortBy);
        }

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), id);

        for (Film film : films) {
            film.setMpa(getMpaFromDataBase(film.getId()));
            film.setGenres(getGenresFromDataBase(film.getId()));
            film.setDirectors(getDirectorsFromDataBase(film.getId()));
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
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, COUNT(l.film_id) as likes_count " +
                    "FROM films f JOIN films_directors fd ON f.film_id = fd.film_id JOIN directors d ON fd.director_id = d.director_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "WHERE LOWER(d.director_name) LIKE LOWER('%" + query + "%') " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration " +
                    "ORDER BY likes_count";
        } else if (Objects.equals(by, "title")) {
            sql = "SELECT films.film_id, films.name, films.description, films.release_date, films.duration, " +
                    "COUNT (likes.film_id) as likes_count " +
                    "FROM films LEFT JOIN likes ON films.film_id = likes.film_id " +
                    "WHERE LOWER(films.name) LIKE LOWER('%" + query + "%') " +
                    "GROUP BY films.film_id, films.name, films.description, films.release_date, films.duration " +
                    "ORDER BY likes_count";
        } else if (Objects.equals(by, "title,director") || Objects.equals(by, "director,title")) {
            sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, COUNT(l.film_id) AS likes_count " +
                    "FROM films f LEFT JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.director_id LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "WHERE LOWER (d.director_name) LIKE LOWER ('%" + query + "%') OR LOWER (f.name) LIKE LOWER ('%" + query + "%') " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration " +
                    "ORDER BY likes_count DESC";
        } else {
            throw new NotFoundException("ALG_2. Invalid RequestParam:  " + by);
        }

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());

        for (Film film : films) {
            film.setMpa(getMpaFromDataBase(film.getId()));
            film.setGenres(getGenresFromDataBase(film.getId()));
            film.setDirectors(getDirectorsFromDataBase(film.getId()));
        }
        return films;
    }

    /**
     * ALG_3
     */
    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        String sql = "SELECT F.FILM_ID, F.name, F.description, F.release_date, F.duration, COUNT(user_id) " +
                "FROM FILMS f LEFT JOIN LIKES l ON F.FILM_ID = L.FILM_ID " +
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
            film.setMpa(getMpaFromDataBase(film.getId()));
            film.setGenres(getGenresFromDataBase(film.getId()));
            film.setDirectors(getDirectorsFromDataBase(film.getId()));
        }
        return films;
    }

    /**
     * ALG_1
     */
    @Override
    public Review addNewReview(Review review) {
        if (review.getReviewId() != null) throw new ValidationException("Поле id у отзыва не пустое");
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName("reviews").usingGeneratedKeyColumns("review_id");
        Map<String, Object> reviewInMap = new HashMap<>();
        reviewInMap.put("content", review.getContent());
        reviewInMap.put("is_positive", review.getIsPositive());
        reviewInMap.put("user_id", review.getUserId());
        reviewInMap.put("film_id", review.getFilmId());
        review.setReviewId(simpleJdbcInsert.executeAndReturnKey(reviewInMap).longValue());
        addEvent(review.getUserId(), "REVIEW", "ADD", review.getReviewId());
        return review;
    }

    /**
     * ALG_1
     */
    @Override
    public Review updateReview(Review review) {
        Review updatedReview = getReviewById(review.getReviewId());
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        updatedReview.setContent(review.getContent());
        updatedReview.setIsPositive(review.getIsPositive());
        addEvent(updatedReview.getUserId(), "REVIEW", "UPDATE", updatedReview.getFilmId());
        return updatedReview;
    }

    /**
     * ALG_1
     */
    @Override
    public void deleteReview(Long reviewId) {
        Review review = getReviewById(reviewId);
        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", reviewId);
        jdbcTemplate.update("DELETE FROM reviews_like WHERE review_id = ?", reviewId);
        addEvent(review.getUserId(), "REVIEW", "REMOVE",
                review.getFilmId());
    }

    /**
     * ALG_1
     */
    @Override
    public Review getReviewById(Long reviewId) {
        String sql = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id," +
                "SUM(CASE rl.is_useful WHEN true THEN 1 WHEN false THEN -1 END) AS score " +
                "FROM reviews r " +
                "LEFT JOIN reviews_like rl ON r.review_id = rl.review_id " +
                "WHERE r.review_id = ? " +
                "GROUP BY r.review_id";
        return jdbcTemplate.queryForObject(sql, reviewRowMapper, reviewId);
    }

    /**
     * ALG_1
     */
    @Override
    public List<Review> getAllReviews() {
        String sql = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, " +
                "SUM(CASE rl.is_useful WHEN true THEN 1 WHEN false THEN -1 END) AS score " +
                "FROM reviews r " +
                "LEFT JOIN reviews_like rl ON r.review_id = rl.review_id " +
                "GROUP BY r.review_id";
        return jdbcTemplate.query(sql, reviewRowMapper);
    }

    /**
     * ALG_1
     */
    @Override
    public void addLikeByReview(Long reviewId, Long userId) {
        String sql = "INSERT INTO reviews_like (review_id, user_id, is_useful) VALUES(?, ?, true)";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    /**
     * ALG_1
     */
    @Override
    public void addDislikeByReview(Long reviewId, Long userId) {
        String sql = "INSERT INTO reviews_like (review_id, user_id, is_useful) VALUES(?, ?, false)";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    /**
     * ALG_1
     */
    @Override
    public void deleteLikeByReview(Long reviewId, Long userId) {
        String sql = "DELETE FROM reviews_like WHERE review_id = ? AND user_id = ? AND is_useful = true";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    /**
     * ALG_1
     */
    @Override
    public void deleteDislikeByReview(Long reviewId, Long userId) {
        String sql = "DELETE FROM reviews_like WHERE review_id = ? AND user_id = ? AND is_useful = false";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    /**
     * ALG5
     */
    private void addEvent(Long userId, String eventType, String operation, Long entityId) {
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
