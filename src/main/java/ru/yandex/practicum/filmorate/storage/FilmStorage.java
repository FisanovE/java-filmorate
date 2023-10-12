package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface FilmStorage {

	Film addNewFilm(Film film);

	Film updateFilm(Film film);

	Collection<Film> getAllFilms();

	Film getFilmById(Long id);

	void addLike(Long id, Long userId);

	void deleteLike(Long id, Long userId);

	Collection<Film> getTopRatingFilms(int count);

	/** ALG_8*/
	Collection<Film> getTopRatingFilmsByGenreAndYear(int count, long genreId, int year);

	Collection<Genre> getAllGenres();

	Genre getGenresById(Long id);

	Collection<Mpa> getAllRatingsMpa();

	Mpa getRatingsMpaById(Long id);
}

