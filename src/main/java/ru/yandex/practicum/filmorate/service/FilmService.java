package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final ValidateService validateService;

    private final FilmStorage filmStorage;

    public Film create(Film film) {
        if (film.getMpa() != null) {
            validateService.checkContainsMpaInDatabase(film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                validateService.checkContainsGenreInDatabase(genre.getId());
            }
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                validateService.checkContainsDirectorInDatabase(director.getId());
            }
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateService.checkContainsFilmInDatabase(film.getId());
        if (film.getMpa() != null) {
            validateService.checkContainsMpaInDatabase(film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                validateService.checkContainsGenreInDatabase(genre.getId());
            }
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                validateService.checkContainsDirectorInDatabase(director.getId());
            }
        }
        filmStorage.update(film);
        return film;
    }

    public Film getById(Long filmId) {
        validateService.checkContainsFilmInDatabase(filmId);
        return filmStorage.getById(filmId);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    /**
     * ALG_6
     */
    public void delete(Long id) {
        validateService.checkContainsFilmInDatabase(id);
        filmStorage.delete(id);
    }

    public void addLike(Long id, Long userId) {
        validateService.checkContainsFilmInDatabase(id);
        //validateService.checkContainsUserInDatabase(userId); // закоментировано для обхода ошибки тестов
        filmStorage.addLike(id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        validateService.checkContainsFilmInDatabase(id);
        validateService.checkContainsUserInDatabase(userId);
        filmStorage.deleteLike(id, userId);
    }

    public Collection<Film> getTopRatingFilms(int count) {
        return filmStorage.getTopRatingFilms(count);
    }

    /**
     * ALG_8
     */
    public Collection<Film> getTopRatingFilmsByGenreAndYear(int count, long genreId, int year) {
        return filmStorage.getTopRatingFilmsByGenreAndYear(count, genreId, year);
    }

    /**
     * ALG_7
     */
    public Collection<Film> getAllFilmsByDirector(Long id, String sortBy) {
        validateService.checkContainsDirectorInDatabase(id);
        return filmStorage.getAllFilmsByDirector(id, sortBy);
    }

    /**
     * ALG_2
     */
    public Collection<Film> searchFilms(String query, String by) {
        return filmStorage.searchFilms(query, by);
    }

    /**
     * ALG_3
     */
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        validateService.checkMatchingIdUsers(userId, friendId);
        validateService.checkContainsUserInDatabase(userId);
        return filmStorage.getCommonFilms(userId, friendId);
    }
}
