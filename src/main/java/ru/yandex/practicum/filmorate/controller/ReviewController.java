package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.service.ValidateService;

import java.util.List;

@Slf4j
@RestController
@Component
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ValidateService validateService;

    /**
     * ALG_1
     */
    @PostMapping
    public Review addNewReview(@RequestBody Review review) {
        log.info("Create review");
        return reviewService.addNewReviews(review);
    }

    /**
     * ALG_1
     */
    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        validateService.checkIdNotNull(review.getReviewId());
        log.info("Update review {}", review.getReviewId());
        return reviewService.updateReview(review);
    }

    /**
     * ALG_1
     */
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        log.info("Delete review {}", id);
        reviewService.deleteReview(id);
    }

    /**
     * ALG_1
     */
    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        log.info("Get review {}", id);
        return reviewService.getReviewById(id);
    }

    /**
     * ALG_1
     */
    @GetMapping
    public List<Review> getReviewsByFilmId(@RequestParam(defaultValue = "0") Long filmId, @RequestParam(defaultValue = "10") Integer count) {
        log.info("Get reviews by filmId {}", filmId);
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    /**
     * ALG_1
     */
    @PutMapping("/{id}/like/{userId}")
    public void addLikeByReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("User {} added Like by Review {}", userId, id);
        reviewService.addLikeByReview(id, userId);
    }

    /**
     * ALG_1
     */
    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeByReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("User {} added Dislike by Review {}", userId, id);
        reviewService.addDislikeByReview(id, userId);
    }

    /**
     * ALG_1
     */
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeByReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("User {} delete Like by Review {}", userId, id);
        reviewService.deleteLikeByReview(id, userId);
    }

    /**
     * ALG_1
     */
    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeByReview(@PathVariable Long id, @PathVariable Long userId) {
        log.info("User {} delete Dislike by Review {}", userId, id);
        reviewService.deleteDislikeByReview(id, userId);
    }
}
