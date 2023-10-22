package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
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
    private final UserService userService;

    public Review create(Review review) {
        if (review.getReviewId() != null) throw new ValidationException("Поле id у отзыва не пустое");
        validateService.checkReview(review);
        validateService.checkContainsUserInDatabase(review.getUserId());
        validateService.checkContainsUserInDatabase(review.getFilmId());
        return reviewDbStorage.create(review);
    }

    public Review update(Review review) {
        validateService.checkContainsReviewInDatabase(review.getReviewId());
        userService.getById(review.getUserId());
        validateService.checkContainsFilmInDatabase(review.getFilmId());
        validateService.checkReview(review);
        reviewDbStorage.update(review);
        return getById(review.getReviewId());
    }

    public void delete(Long reviewId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        reviewDbStorage.delete(reviewId);
    }

    public Review getById(Long reviewId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        return reviewDbStorage.getById(reviewId);
    }

    public List<Review> getByFilmId(Long filmId, Integer count) {
        return reviewDbStorage.getByFilmId(filmId, count)
                .stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        userService.getById(userId);
        reviewDbStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        userService.getById(userId);
        reviewDbStorage.addDislike(reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        userService.getById(userId);
        reviewDbStorage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        validateService.checkContainsReviewInDatabase(reviewId);
        userService.getById(userId);
        reviewDbStorage.deleteDislike(reviewId, userId);
    }

}
