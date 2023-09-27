package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.impl.InMemoryFilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@RequestMapping ("/films")
public class FilmService {

	@Qualifier ("filmDbStorage")
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
}
