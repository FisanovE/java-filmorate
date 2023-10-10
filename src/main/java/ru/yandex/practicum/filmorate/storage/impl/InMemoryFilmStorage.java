package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Repository
public class InMemoryFilmStorage implements FilmStorage {

	private final Map<Long, Film> films = new HashMap<>();
	private final Map<Long, Genre> genres = Map.of(
			1L, Genre.builder().id(1L).name("Комедия").build(),
			2L, Genre.builder().id(2L).name("Драма").build(),
			3L, Genre.builder().id(3L).name("Мультфильм").build(),
			4L, Genre.builder().id(4L).name("Триллер").build(),
			5L, Genre.builder().id(5L).name("Документальный").build(),
			6L, Genre.builder().id(6L).name("Боевик").build()
	);
	private final Map<Long, Mpa> ratingsMpa = Map.of(
			1L, Mpa.builder().id(1L).name("G").build(),
			2L, Mpa.builder().id(2L).name("PG").build(),
			3L, Mpa.builder().id(3L).name("PG-13").build(),
			4L, Mpa.builder().id(4L).name("R").build(),
			5L, Mpa.builder().id(5L).name("NC-17").build()
	);
	private Long counter = 1L;

	@Override
	public Film addNewFilm(Film film) {
		checkingRepeat(films, film);
		film.setId(counter);
		counter++;
		films.put(film.getId(), film);
		log.info("Film added: {}.", film);
		return film;
	}

	@Override
	public Film updateFilm(Film film) {
		if (!films.containsKey(film.getId())) {
			throw new NotFoundException("Invalid Film ID:  " + film.getId());
		}
		checkingRepeat(films, film);
		films.put(film.getId(), film);
		log.info("Film updated: {}.", film);
		return film;
	}

	@Override
	public Film getFilmById(Long id) {
		if (films.containsKey(id)) {
			return films.get(id);
		}
		throw new NotFoundException("Invalid Film ID:  " + id);
	}

	@Override
	public Collection<Film> getAllFilms() {
		return films.values();
	}

	private void checkingRepeat(Map<Long, Film> films, Film film) {
		for (Film currentFilm : films.values()) {
			if (Objects.equals(currentFilm.getName(), film.getName()) && Objects.equals(currentFilm.getReleaseDate(), film.getReleaseDate()) && !Objects.equals(currentFilm.getId(), film.getId())) {
				throw new ValidationException("This information for the movie " + film.getName() + " is already available.");
			}
		}
	}

	@Override
	public void addLike(Long id, Long userId) {
		Film filmLiked = getFilmById(id);
		if (filmLiked.getLikedUsersIds() == null) {
			filmLiked.setLikedUsersIds(Stream.of(userId).collect(Collectors.toCollection(ArrayList::new)));
		} else {
			List<Long> set = filmLiked.getLikedUsersIds();
			set.add(userId);
			filmLiked.setLikedUsersIds(set);
		}
		updateFilm(filmLiked);
	}

	@Override
	public void deleteLike(Long id, Long userId) {
		if (getFilmById(id).getLikedUsersIds() == null || !getFilmById(id).getLikedUsersIds().contains(userId)) {
			throw new NotFoundException("User ID is missing from likes:  " + userId);
		} else {
			Film filmLiked = getFilmById(id);
			List<Long> idUsers = filmLiked.getLikedUsersIds();
			idUsers.remove(userId);
			filmLiked.setLikedUsersIds(idUsers);
			updateFilm(filmLiked);
		}
	}

	Comparator<Film> filmComparator = (film1, film2) -> {
		if (film1.getLikedUsersIds() == null || film1.getLikedUsersIds().isEmpty()) {
			return 1;
		} else if (film2.getLikedUsersIds() == null || film2.getLikedUsersIds().isEmpty()) {
			return -1;
		}
		return film2.getLikedUsersIds().size() - film1.getLikedUsersIds().size();
	};

	@Override
	public Collection<Film> getTopRatingFilms(int count) {
		return getAllFilms().stream().sorted(filmComparator).limit(count).collect(Collectors.toSet());
	}

	@Override
	public Collection<Genre> getAllGenres() {
		return genres.values().stream().sorted(Comparator.comparing(Genre::getId)).collect(Collectors.toList());
	}

	@Override
	public Genre getGenresById(Long id) {
		if (genres.containsKey(id)) {
			return genres.get(id);
		} else {
			throw new NotFoundException("Genre ID is missing:  " + id);
		}
	}

	@Override
	public Collection<Mpa> getAllRatingsMpa() {
		return ratingsMpa.values().stream().sorted(Comparator.comparing(Mpa::getId)).collect(Collectors.toList());
	}

	@Override
	public Mpa getRatingsMpaById(Long id) {
		if (ratingsMpa.containsKey(id)) {
			return ratingsMpa.get(id);
		} else {
			throw new NotFoundException("Ratings Mpa ID is missing:  " + id);
		}
	}

	@Override
	public void deleteFilm(Long filmId) {
	}
}
