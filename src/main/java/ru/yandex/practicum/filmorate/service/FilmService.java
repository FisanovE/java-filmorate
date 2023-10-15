package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public Film addNewFilm(Film film) {
        return filmStorage.addNewFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(Long id, Long userId) {
        filmStorage.addLike(id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        filmStorage.deleteLike(id, userId);
    }

    public Collection<Film> getTopRatingFilms(int count) {
        return filmStorage.getTopRatingFilms(count);
    }

    /**
     * ALG_8
     */
    public Collection<Film> getTopRatingFilmsByGenreAndYear(int count, long genreId, int year) {
        log.debug("ALG_8.FilmService -> entered into service");
        return filmStorage.getTopRatingFilmsByGenreAndYear(count, genreId, year);
    }

    public Collection<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public Genre getGenresById(Long id) {
        return filmStorage.getGenresById(id);
    }

    public Collection<Mpa> getAllRatingsMpa() {
        return filmStorage.getAllRatingsMpa();
    }

    public Mpa getRatingsMpaById(Long id) {
        return filmStorage.getRatingsMpaById(id);
    }

    /**
     * ALG_7
     */
    public Collection<Film> getAllFilmsByDirector(Long id, String sortBy) {
        return filmStorage.getAllFilmsByDirector(id, sortBy);
    }

    /**
     * ALG_2
     */
    public Collection<Film> searchFilms(String query, String by) {
        return filmStorage.searchFilms(query, by);
    }

    /**
     * ALG_6
     */
    public void deleteFilm(Long id) {
        filmStorage.deleteFilm(id);
    }

    /**
     * ALG_3
     */
    public Collection<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public Review addNewReviews(Review review) {
        checkReview(review);
        return filmStorage.addNewReview(review);
    }

    public Review updateReview(Review review) {
        checkReview(review);
        return filmStorage.updateReview(review);
    }

    public void deleteReview(Long reviewId) {
        filmStorage.deleteReview(reviewId);
    }

    public Review getReviewById(Long reviewId) {
        return filmStorage.getReviewById(reviewId);
    }

    public List<Review> getReviewsByFilmId(Long filmId, Integer count) {
        return filmStorage.getAllReviews()
                .stream()
                .filter(review -> (filmId == 0 || review.getFilmId() == filmId))
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLikeByReview(Long reviewId, Long userId) {
        filmStorage.addLikeByReview(reviewId, userId);
    }

    public void addDislikeByReview(Long reviewId, Long userId) {
        filmStorage.addDislikeByReview(reviewId, userId);
    }

    public void deleteLikeByReview(Long reviewId, Long userId) {
        filmStorage.deleteLikeByReview(reviewId, userId);
    }

    public void deleteDislikeByReview(Long reviewId, Long userId) {
        filmStorage.deleteDislikeByReview(reviewId, userId);
    }

    private void checkReview(Review review) {
        if (review.getContent() == null) throw new ValidationException("Отзыв не может быть пустой");
        if (review.getIsPositive() == null) throw new ValidationException("Тип отзыва не может быть пустой");
        if (review.getUserId() == null) throw new ValidationException("Некорректный Id пользователя");
        userStorage.getUserById(review.getUserId());
        if (review.getFilmId() == null) throw new ValidationException("Некорректный Id фильма");
        filmStorage.getFilmById(review.getFilmId());
    }
}
