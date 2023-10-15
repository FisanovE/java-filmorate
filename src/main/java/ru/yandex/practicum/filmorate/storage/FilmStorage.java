package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Film addNewFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getAllFilms();

    Film getFilmById(Long id);

    void addLike(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    Collection<Film> getTopRatingFilms(int count);

    /**
     * ALG_8
     */
    Collection<Film> getTopRatingFilmsByGenreAndYear(int count, long genreId, int year);

    Collection<Genre> getAllGenres();

    Genre getGenresById(Long id);

    Collection<Mpa> getAllRatingsMpa();

    Mpa getRatingsMpaById(Long id);

    /**
     * ALG_7
     */
    Collection<Film> getAllFilmsByDirector(Long id, String sortBy);

    /**
     * ALG_6
     */
    void deleteFilm(Long filmId);

    /**
     * ALG_2
     */
    Collection<Film> searchFilms(String query, String by);

	/**
	 * ALG_3
	 */
    Collection<Film> getCommonFilms(Long userId, Long friendId);

    Review addNewReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Long reviewId);

    Review getReviewById(Long reviewId);

    List<Review> getAllReviews();

    void addLikeByReview(Long reviewId, Long userId);

    void addDislikeByReview(Long reviewId, Long userId);

    void deleteLikeByReview(Long reviewId, Long userId);

    void deleteDislikeByReview(Long reviewId, Long userId);
}

