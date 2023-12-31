package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.*;

/**
 * ALG_7
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcOperations jdbcOperations;
    private final ReviewRowMapper reviewRowMapper = new ReviewRowMapper();
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    /**
     * ALG_1
     */
    @Override
    public Review create(Review review) {
        userDbStorage.checkContainsUser(review.getUserId());
        filmDbStorage.checkContainsFilm(review.getFilmId());
        if (review.getReviewId() != null) throw new ValidationException("Поле id у отзыва не пустое");
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert((JdbcTemplate) jdbcOperations);
        simpleJdbcInsert.withTableName("reviews").usingGeneratedKeyColumns("review_id");
        Map<String, Object> reviewInMap = new HashMap<>();
        reviewInMap.put("content", review.getContent());
        reviewInMap.put("is_positive", review.getIsPositive());
        reviewInMap.put("user_id", review.getUserId());
        reviewInMap.put("film_id", review.getFilmId());
        review.setReviewId(simpleJdbcInsert.executeAndReturnKey(reviewInMap).longValue());

        return review;
    }

    /**
     * ALG_1
     */
    @Override
    public void update(Review review) {
        userDbStorage.checkContainsUser(review.getUserId());
        filmDbStorage.checkContainsFilm(review.getFilmId());
        Review updatedReview = getById(review.getReviewId());
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int rows = jdbcOperations.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        updatedReview.setContent(review.getContent());
        updatedReview.setIsPositive(review.getIsPositive());
        if (rows == 0) {
            throw new NotFoundException("Invalid Review ID:  " + review.getReviewId());
        }
    }

    /**
     * ALG_1
     */
    @Override
    public Review delete(Long reviewId) {
        Review review = getById(reviewId);
        int rows = jdbcOperations.update("DELETE FROM reviews WHERE review_id = ?", reviewId);
        if (rows == 0) {
            throw new NotFoundException("Invalid Review ID:  " + review.getReviewId());
        }
        jdbcOperations.update("DELETE FROM reviews_like WHERE review_id = ?", reviewId);
        return review;
    }

    /**
     * ALG_1
     */
    @Override
    public Review getById(Long reviewId) {
        String sql = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id," +
                "SUM(CASE rl.is_useful WHEN true THEN 1 WHEN false THEN -1 END) AS score " +
                "FROM reviews r " +
                "LEFT JOIN reviews_like rl ON r.review_id = rl.review_id " +
                "WHERE r.review_id = ? " +
                "GROUP BY r.review_id";
        return jdbcOperations
                .query(sql, reviewRowMapper, reviewId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Review with id = " + reviewId + " not found"));
    }

    /**
     * ALG_1
     */
    @Override
    public List<Review> getByFilmId(Long filmId, Integer count) {
        String sql;
        if (filmId == 0) {
            sql = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, " +
                    "SUM(CASE rl.is_useful WHEN true THEN 1 WHEN false THEN -1 END) AS score " +
                    "FROM reviews r " +
                    "LEFT JOIN reviews_like rl ON r.review_id = rl.review_id " +
                    "GROUP BY r.review_id " +
                    "LIMIT ?";
            return jdbcOperations.query(sql, reviewRowMapper, count);
        } else {
            sql = "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, " +
                    "SUM(CASE rl.is_useful WHEN true THEN 1 WHEN false THEN -1 END) AS score " +
                    "FROM reviews r " +
                    "LEFT JOIN reviews_like rl ON r.review_id = rl.review_id " +
                    "WHERE r.film_id = ?" +
                    "GROUP BY r.review_id " +
                    "LIMIT ?";
            return jdbcOperations.query(sql, reviewRowMapper, filmId, count);
        }
    }

    /**
     * ALG_1
     */
    @Override
    public void addLike(Long reviewId, Long userId) {
        userDbStorage.checkContainsUser(userId);
        String sql = "INSERT INTO reviews_like (review_id, user_id, is_useful) VALUES(?, ?, true)";
        int rows = jdbcOperations.update(sql, reviewId, userId);
        if (rows == 0) {
            throw new NotFoundException("Invalid Review ID:  " + reviewId);
        }
    }

    /**
     * ALG_1
     */
    @Override
    public void addDislike(Long reviewId, Long userId) {
        userDbStorage.checkContainsUser(userId);
        String sql = "INSERT INTO reviews_like (review_id, user_id, is_useful) VALUES(?, ?, false)";
        int rows = jdbcOperations.update(sql, reviewId, userId);
        if (rows == 0) {
            throw new NotFoundException("Invalid Review ID:  " + reviewId);
        }
    }

    /**
     * ALG_1
     */
    @Override
    public void deleteLike(Long reviewId, Long userId) {
        userDbStorage.checkContainsUser(userId);
        String sql = "DELETE FROM reviews_like WHERE review_id = ? AND user_id = ? AND is_useful = true";
        int rows = jdbcOperations.update(sql, reviewId, userId);
        if (rows == 0) {
            throw new NotFoundException("Invalid Review ID:  " + reviewId);
        }
    }

    /**
     * ALG_1
     */
    @Override
    public void deleteDislike(Long reviewId, Long userId) {
        userDbStorage.checkContainsUser(userId);
        String sql = "DELETE FROM reviews_like WHERE review_id = ? AND user_id = ? AND is_useful = false";
        int rows = jdbcOperations.update(sql, reviewId, userId);
        if (rows == 0) {
            throw new NotFoundException("Invalid Review ID:  " + reviewId);
        }
    }
}
