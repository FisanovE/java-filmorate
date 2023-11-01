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
    public Review create(@RequestBody Review review) {
        log.info("Create review");
        return reviewService.create(review);
    }

    /**
     * ALG_1
     */
    @PutMapping
    public Review update(@RequestBody Review review) {
        validateService.checkIdNotNull(review.getReviewId());
        log.info("Update review {}", review.getReviewId());
        return reviewService.update(review);
    }

    /**
     * ALG_1
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Delete review {}", id);
        reviewService.delete(id);
    }

    /**
     * ALG_1
     */
    @GetMapping("/{id}")
    public Review getById(@PathVariable Long id) {
        log.info("Get review {}", id);
        return reviewService.getById(id);
    }

    /**
     * ALG_1
     */
    @GetMapping
    public List<Review> getByFilmId(@RequestParam(defaultValue = "0") Long filmId, @RequestParam(defaultValue = "10") Integer count) {
        log.info("Get reviews by filmId {}", filmId);
        return reviewService.getByFilmId(filmId, count);
    }

    /**
     * ALG_1
     */
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("User {} added Like by Review {}", userId, id);
        reviewService.addLike(id, userId);
    }

    /**
     * ALG_1
     */
    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("User {} added Dislike by Review {}", userId, id);
        reviewService.addDislike(id, userId);
    }

    /**
     * ALG_1
     */
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("User {} delete Like by Review {}", userId, id);
        reviewService.deleteLike(id, userId);
    }

    /**
     * ALG_1
     */
    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("User {} delete Dislike by Review {}", userId, id);
        reviewService.deleteDislike(id, userId);
    }
}
