package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	private final InMemoryFilmStorage filmStorage;

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
		if (filmLiked.getLikedUsersIds() == null) {
			filmLiked.setLikedUsersIds(Stream.of(userId).collect(Collectors.toCollection(HashSet::new)));
		} else {
			Set<Long> set = filmLiked.getLikedUsersIds();
			set.add(userId);
			filmLiked.setLikedUsersIds(set);
		}
		filmStorage.updateFilm(filmLiked);
	}

	public void deleteLike(Long id, Long userId) {
		if (filmStorage.getFilmById(id).getLikedUsersIds() == null || !filmStorage.getFilmById(id).getLikedUsersIds()
																				  .contains(userId)) {
			throw new NotFoundException("User ID is missing from likes:  " + userId);
		} else {
			Film filmLiked = filmStorage.getFilmById(id);
			Set<Long> idUsers = filmLiked.getLikedUsersIds();
			idUsers.remove(userId);
			filmLiked.setLikedUsersIds(idUsers);
			filmStorage.updateFilm(filmLiked);
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

	public Collection<Film> getTopRatingFilms(int count) {
		return filmStorage.getAllFilms().stream().sorted(filmComparator).limit(count)
						  .collect(Collectors.toSet());
	}
}
