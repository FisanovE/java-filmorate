package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.impl.ReviewDbStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewDbStorage reviewDbStorage;
    private final ValidateService validateService;
    private final EventStorage eventStorage;

    public Review create(Review review) {
        validateService.checkReview(review);
        validateService.checkContainsUserInDatabase(review.getUserId());
        validateService.checkContainsUserInDatabase(review.getFilmId());
        Review created = reviewDbStorage.create(review);
        eventStorage.addEvent(created.getUserId(), "REVIEW", "ADD", created.getReviewId());
        return created;
    }

    public Review update(Review review) {
        validateService.checkContainsReviewInDatabase(review.getReviewId());
        validateService.checkContainsUserInDatabase(review.getUserId());
        validateService.checkContainsUserInDatabase(review.getFilmId());
        validateService.checkReview(review);
        Review updated = reviewDbStorage.update(review);
        eventStorage.addEvent(updated.getUserId(), "REVIEW", "UPDATE", updated.getFilmId());
        return updated;
    }

    public void delete(Long reviewId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        Review deleted = reviewDbStorage.delete(reviewId);
        eventStorage.addEvent(deleted.getUserId(), "REVIEW", "REMOVE", deleted.getFilmId());
    }

    public Review getById(Long reviewId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        return reviewDbStorage.getById(reviewId);
    }

    public List<Review> getByFilmId(Long filmId, Integer count) {
        return reviewDbStorage.getAll()
                .stream()
                .filter(review -> (filmId == 0 || Objects.equals(review.getFilmId(), filmId)))
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        validateService.checkContainsUserInDatabase(userId);
        reviewDbStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        validateService.checkContainsUserInDatabase(userId);
        reviewDbStorage.addDislike(reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        validateService.checkContainsUserInDatabase(userId);
        reviewDbStorage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        validateService.checkContainsUserInDatabase(userId);
        reviewDbStorage.deleteDislike(reviewId, userId);
    }

}
