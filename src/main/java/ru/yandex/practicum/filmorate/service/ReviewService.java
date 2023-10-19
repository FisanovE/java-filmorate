package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final FilmStorage filmStorage;
    private final ValidateService validateService;

    public Review create(Review review) {
        validateService.checkReview(review);
        validateService.checkContainsUserInDatabase(review.getUserId());
        validateService.checkContainsUserInDatabase(review.getFilmId());
        return filmStorage.createReview(review);
    }

    public Review update(Review review) {
        validateService.checkContainsReviewInDatabase(review.getReviewId());
        validateService.checkContainsUserInDatabase(review.getUserId());
        validateService.checkContainsUserInDatabase(review.getFilmId());
        validateService.checkReview(review);
        return filmStorage.updateReview(review);
    }

    public void delete(Long reviewId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        filmStorage.deleteReview(reviewId);
    }

    public Review getById(Long reviewId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        return filmStorage.getReviewById(reviewId);
    }

    public List<Review> getByFilmId(Long filmId, Integer count) {
        return filmStorage.getAllReviews()
                .stream()
                .filter(review -> (filmId == 0 || Objects.equals(review.getFilmId(), filmId)))
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        validateService.checkContainsUserInDatabase(userId);
        filmStorage.addLikeByReview(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        validateService.checkContainsUserInDatabase(userId);
        filmStorage.addDislikeByReview(reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        validateService.checkContainsUserInDatabase(userId);
        filmStorage.deleteLikeByReview(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        validateService.checkContainsUserInDatabase(userId);
        filmStorage.deleteDislikeByReview(reviewId, userId);
    }

}
