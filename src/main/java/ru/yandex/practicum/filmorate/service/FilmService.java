package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

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

	InMemoryFilmStorage filmStorage;

	@Autowired
	public FilmService(InMemoryFilmStorage filmStorage) {
		this.filmStorage = filmStorage;
	}

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
		Film filmLiked = filmStorage.getFilmById(id);
		if (filmLiked.getLikes() == null) {
			filmLiked.setLikes(Stream.of(userId).collect(Collectors.toCollection(HashSet :: new)));
		} else {
			Set<Long> set = filmLiked.getLikes();
			set.add(userId);
			filmLiked.setLikes(set);
		}
		filmStorage.updateFilm(filmLiked);
	}

	public void deleteLike(Long id, Long userId) {
		if (filmStorage.getFilmById(id).getLikes() == null || !filmStorage.getFilmById(id).getLikes()
																		  .contains(userId)) {
			throw new NotFoundException("User ID is missing from likes:  " + userId);
		} else {
			Film filmLiked = filmStorage.getFilmById(id);
			Set<Long> idUsers = filmLiked.getLikes();
			idUsers.remove(userId);
			filmLiked.setLikes(idUsers);
			filmStorage.updateFilm(filmLiked);
		}
	}

	Comparator<Film> filmComparator = (film1, film2) -> {
		if (film1.getLikes() == null || film1.getLikes().isEmpty()) {
			return 1;
		} else if (film2.getLikes() == null || film2.getLikes().isEmpty()) {
			return -1;
		}
		return film2.getLikes().size() - film1.getLikes().size();
	};

	public Collection<Film> getTopRatingFilms(int count) {
		return filmStorage.getAllFilms().stream().sorted(filmComparator).limit(count)
						  .collect(Collectors.toSet());
	}
}
