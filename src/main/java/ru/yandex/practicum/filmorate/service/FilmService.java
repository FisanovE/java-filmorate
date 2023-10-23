package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenresStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final ValidateService validateService;

    private final FilmStorage filmStorage;

    private final DirectorStorage directorStorage;

    private final GenresStorage genresStorage;

    private final EventStorage eventStorage;

    public Film create(Film film) {
        validateService.checkingFilmForValid(film);
        List<Film> withId = List.of(filmStorage.create(film));
        directorStorage.save(withId);
        genresStorage.save(withId);
        return withId.get(0);
    }

    public Film update(Film film) {
        validateService.checkingFilmForValid(film);
        filmStorage.update(film);
        List<Film> updated = List.of(film);
        genresStorage.save(updated);
        directorStorage.save(updated);

        return film;
    }

    /**
     * ALG_6
     */
    public void delete(Long id) {
        filmStorage.delete(id);
    }

    public Film getById(Long filmId) {
        List<Film> film = List.of(filmStorage.getById(filmId));
        genresStorage.load(film);
        directorStorage.load(film);
        return film.get(0);
    }

    public Collection<Film> getAll() {
        Collection<Film> films = filmStorage.getAll();
        genresStorage.load(films);
        directorStorage.load(films);
        return films;
    }

    public void addLike(Long id, Long userId) {
        filmStorage.addLike(id, userId);
        eventStorage.create(userId, "LIKE", "ADD", id);
    }

    public void deleteLike(Long id, Long userId) {
        filmStorage.deleteLike(id, userId);
        eventStorage.create(userId, "LIKE", "REMOVE", id);
    }

    /**
     * ALG_8
     */
    public Collection<Film> getTopRatingFilmsByGenreAndYear(int count, long genreId, int year) {
        Collection<Film> films = filmStorage.getTopRatingFilmsByGenreAndYear(count, genreId, year);
        genresStorage.load(films);
        directorStorage.load(films);
        return films;

    }

    /**
     * ALG_7
     */
    public Collection<Film> getAllFilmsByDirector(Long id, SortParameter sortBy) {
        Collection<Film> films = filmStorage.getAllFilmsByDirector(id, sortBy);
        genresStorage.load(films);
        directorStorage.load(films);
        return films;
    }

    /**
     * ALG_2
     */
    public Collection<Film> searchFilms(String query, List<SearchParameter> by) {
        Collection<Film> films = filmStorage.searchFilms(query, by);
        genresStorage.load(films);
        directorStorage.load(films);
        return films;
    }

    /**
     * ALG_3
     */
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        validateService.checkMatchingIdUsers(userId, friendId);
        Collection<Film> films = filmStorage.getCommonFilms(userId, friendId);
        genresStorage.load(films);
        directorStorage.load(films);
        return films;
    }

    /**
     * ALG_4
     */
    public Collection<Film> getRecommendationsForUser(Long id) {
        Collection<Film> films = filmStorage.getFilmsRecommendationsForUser(id);
        genresStorage.load(films);
        directorStorage.load(films);
        return films;
    }
}
