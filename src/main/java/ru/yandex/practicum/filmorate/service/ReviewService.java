package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.impl.ReviewDbStorage;

import java.util.Comparator;
import java.util.List;
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
        Review created = reviewDbStorage.create(review);
        eventStorage.create(created.getUserId(), EventType.REVIEW, OperationType.ADD, created.getReviewId());
        return created;
    }

    public Review update(Review review) {
        validateService.checkReview(review);
        reviewDbStorage.update(review);
        eventStorage.create(review.getReviewId(), EventType.REVIEW, OperationType.UPDATE, review.getReviewId());
        return getById(review.getReviewId());
    }

    public void delete(Long reviewId) {
        Review deleted = reviewDbStorage.delete(reviewId);
        eventStorage.create(deleted.getUserId(), EventType.REVIEW, OperationType.REMOVE, deleted.getFilmId());
    }

    public Review getById(Long reviewId) {
        return reviewDbStorage.getById(reviewId);
    }

    public List<Review> getByFilmId(Long filmId, Integer count) {
        return reviewDbStorage.getByFilmId(filmId, count)
                .stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {
        reviewDbStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        reviewDbStorage.addDislike(reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        reviewDbStorage.deleteLike(reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        reviewDbStorage.deleteDislike(reviewId, userId);
    }

}
