package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;


@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    //@Qualifier ("inMemoryFilmStorage")
    private final FilmStorage filmStorage;

    public Film addNewFilm(Film film) {
        return filmStorage.addNewFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(Long filmId) {
        checkFilmExist(filmId);
        return filmStorage.getFilmById(filmId);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(Long id, Long userId) {
        checkFilmExist(id);
        filmStorage.addLike(id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        checkFilmExist(id);
        filmStorage.deleteLike(id, userId);
    }

    public Collection<Film> getTopRatingFilms(int count) {
        return filmStorage.getTopRatingFilms(count);
    }

    public Collection<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public Genre getGenresById(Long id) {
        checkFilmExist(id);
        return filmStorage.getGenresById(id);
    }

    public Collection<Mpa> getAllRatingsMpa() {
        return filmStorage.getAllRatingsMpa();
    }

    public Mpa getRatingsMpaById(Long id) {
        checkFilmExist(id);
        return filmStorage.getRatingsMpaById(id);
    }

    public void deleteFilm(Long id) {
        filmStorage.deleteFilm(id);
    }

    private void checkFilmExist(Long id) {
        if (filmStorage.getFilmById(id) == null) {
            throw new NotFoundException("There is no such film!");
        }
    }

}
