package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utils.DateUtils;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping ("/films")
public class FilmController {

	private final Map<Integer, Film> films = new HashMap<>();
	private int counter = 1;


	@PostMapping
	public Film addNewFilm(@Valid @RequestBody Film film) throws ValidationException {
		log.info("Received request to endpoint: POST /films");
		checkingFilmForValid(film);
		film.setId(counter);
		counter++;
		films.put(film.getId(), film);
		log.info("Film added: {}.", film);
		return films.get(film.getId());
	}


	@PutMapping
	public Film updateFilm(@Valid @RequestBody Film film) throws ValidationException {
		log.info("Received request to endpoint: PUT /films");
		checkingFilmForValid(film);
		for (Film currentFilm : films.values()) {
			if (currentFilm.getName().equals(film.getName()) && currentFilm.getReleaseDate()
																		   .equals(film.getReleaseDate())) {
				throw new ValidationException("This information for the movie " + film.getName() + " is already available.");
			}
			films.put(currentFilm.getId(), film);
		}
		log.info("Film updated: {}.", film);
		return films.get(film.getId());
	}


	@GetMapping
	public Collection<Film> getAllFilms() {
		log.info("Received request to endpoint: GET /films");
		return films.values();
	}

	private void checkingFilmForValid(Film film) throws ValidationException {
		if (film.getName().isBlank()) {
			throw new ValidationException("Invalid title format: \"" + film.getName() + "\"");
		}
		if (film.getDescription().length() > 200) {
			throw new ValidationException("The maximum description length is 200 characters, you have: \"" + film.getDescription()
																												 .length() + "\" characters");
		}
		if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28", DateUtils.formatter))) {
			throw new ValidationException("Movie release date should not be earlier than 1895.12.28, you have: \"" + film.getReleaseDate() + "\"");
		}
		if (film.getDuration() < 0) {
			throw new ValidationException("The duration of the film should be positive, you have:  \"" + film.getDuration());
		}
	}

}
