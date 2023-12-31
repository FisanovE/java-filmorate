package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.SearchParameter;
import ru.yandex.practicum.filmorate.model.enums.SortParameter;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    void update(Film film);

    Collection<Film> getAll();

    Film getById(Long id);

    void addLike(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    /**
     * ALG_8
     */
    Collection<Film> getTopRatingFilmsByGenreAndYear(int count, long genreId, int year);

    /**
     * ALG_7
     */
    Collection<Film> getAllFilmsByDirector(Long id, SortParameter sortBy);

    /**
     * ALG_6
     */
    void delete(Long filmId);

    /**
     * ALG_2
     */
    Collection<Film> searchFilms(String query, List<SearchParameter> by);

    /**
     * ALG_3
     */
    Collection<Film> getCommonFilms(Long userId, Long friendId);

    /**
     * ALG_4
     */
    Collection<Film> getFilmsRecommendationsForUser(Long id);
}

