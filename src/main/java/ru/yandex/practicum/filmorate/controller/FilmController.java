package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping ("/films")
public class FilmController {

	private final Map<Integer, Film> films = new HashMap<>();
	private int counter = 1;


	@PostMapping
	public Film addNewFilm(@Valid @RequestBody Film film) {
		log.info("Received request to endpoint: POST /films");
		checkingRepeat(films, film);
		film.setId(counter);
		counter++;
		films.put(film.getId(), film);
		log.info("Film added: {}.", film);
		return film;
	}

	@PutMapping
	public Film updateFilm(@Valid @RequestBody Film film) {
		log.info("Received request to endpoint: PUT /films");
		checkingRepeat(films, film);
		films.put(film.getId(), film);
		log.info("Film updated: {}.", film);
		return film;
	}

	@GetMapping
	public Collection<Film> getAllFilms() {
		log.info("Received request to endpoint: GET /films");
		return films.values();
	}

	private void checkingRepeat(Map<Integer, Film> films, Film film) {
		for (Film currentFilm : films.values()) {
			if (Objects.equals(currentFilm.getName(), film.getName()) && Objects.equals(currentFilm.getReleaseDate(),
					film.getReleaseDate()) && !Objects.equals(currentFilm.getId(), film.getId())) {
				throw new ValidationException("This information for the movie " + film.getName() + " is already available.");
			}
		}
	}
}
