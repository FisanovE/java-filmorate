package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

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

	/**
	 * ALG_4
	 */
	public List<Film> getFilmsRecommendationsForUser(Long id) {
		return filmStorage.getFilmsRecommendationsForUser(id);
	}
}
