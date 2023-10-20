package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
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

    private final JdbcTemplate jdbcTemplate;
    private final FilmDbStorage filmDbStorage;
    private final ReviewRowMapper reviewRowMapper = new ReviewRowMapper();

    /**
     * ALG_1
     */
    @Override
    public Review create(Review review) {
        if (review.getReviewId() != null) throw new ValidationException("Поле id у отзыва не пустое");
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        simpleJdbcInsert.withTableName("reviews").usingGeneratedKeyColumns("review_id");
        Map<String, Object> reviewInMap = new HashMap<>();
        reviewInMap.put("content", review.getContent());
        reviewInMap.put("is_positive", review.getIsPositive());
        reviewInMap.put("user_id", review.getUserId());
        reviewInMap.put("film_id", review.getFilmId());
        review.setReviewId(simpleJdbcInsert.executeAndReturnKey(reviewInMap).longValue());
        filmDbStorage.addEvent(review.getUserId(), "REVIEW", "ADD", review.getReviewId());
        return review;
    }

    /**
     * ALG_1
     */
    @Override
    public Review update(Review review) {
        Review updatedReview = getById(review.getReviewId());
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        updatedReview.setContent(review.getContent());
        updatedReview.setIsPositive(review.getIsPositive());
        filmDbStorage.addEvent(updatedReview.getUserId(), "REVIEW", "UPDATE", updatedReview.getFilmId());
        return updatedReview;
    }

    /**
     * ALG_1
     */
    @Override
    public void delete(Long reviewId) {
        Review review = getById(reviewId);
        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", reviewId);
        jdbcTemplate.update("DELETE FROM reviews_like WHERE review_id = ?", reviewId);
        filmDbStorage.addEvent(review.getUserId(), "REVIEW", "REMOVE",
                review.getFilmId());
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
        return jdbcTemplate.queryForObject(sql, reviewRowMapper, reviewId);
    }

    /**
     * ALG_1
     */
    @Override
    public List<Review> getAll() {
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
    public void addLike(Long reviewId, Long userId) {
        String sql = "INSERT INTO reviews_like (review_id, user_id, is_useful) VALUES(?, ?, true)";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    /**
     * ALG_1
     */
    @Override
    public void addDislike(Long reviewId, Long userId) {
        String sql = "INSERT INTO reviews_like (review_id, user_id, is_useful) VALUES(?, ?, false)";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    /**
     * ALG_1
     */
    @Override
    public void deleteLike(Long reviewId, Long userId) {
        String sql = "DELETE FROM reviews_like WHERE review_id = ? AND user_id = ? AND is_useful = true";
        jdbcTemplate.update(sql, reviewId, userId);
    }

    /**
     * ALG_1
     */
    @Override
    public void deleteDislike(Long reviewId, Long userId) {
        String sql = "DELETE FROM reviews_like WHERE review_id = ? AND user_id = ? AND is_useful = false";
        jdbcTemplate.update(sql, reviewId, userId);
    }
}
