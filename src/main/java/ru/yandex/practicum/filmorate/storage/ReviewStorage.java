package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

/**
 * ALG_1
 */
public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    Review delete(Long reviewId);

    Review getById(Long reviewId);

    List<Review> getAll();

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void deleteLike(Long reviewId, Long userId);

    void deleteDislike(Long reviewId, Long userId);

    SqlRowSet getReviewRow(Long id);
}

