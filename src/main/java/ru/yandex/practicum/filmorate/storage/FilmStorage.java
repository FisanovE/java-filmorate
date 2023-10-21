package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film create(Film film);

    void update(Film film);

    Collection<Film> getAll();

    Film getById(Long id);

    void addLike(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    Collection<Film> getTopRatingFilms(int count);

    /**
     * ALG_8
     */
    Collection<Film> getTopRatingFilmsByGenreAndYear(int count, long genreId, int year);

    /**
     * ALG_7
     */
    Collection<Film> getAllFilmsByDirector(Long id, String sortBy);

    /**
     * ALG_6
     */
    void delete(Long filmId);

    /**
     * ALG_2
     */
    Collection<Film> searchFilms(String query, String by);

    /**
     * ALG_3
     */
    Collection<Film> getCommonFilms(Long userId, Long friendId);

    /**
     * ALG5
     */
    void addEvent(Long userId, String eventType, String operation, Long entityId);

    /**
     * ALG_4
     */
    Collection<Film> getFilmsRecommendationsForUser(Long id);
}

