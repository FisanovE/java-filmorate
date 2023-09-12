package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

	private final Map<Long, Film> films = new HashMap<>();
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
}
