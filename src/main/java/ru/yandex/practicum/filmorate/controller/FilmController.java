package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.utils.DateUtils;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@RestController
@Component
@RequiredArgsConstructor
@RequestMapping ("/films")
public class FilmController {

	private final FilmService filmService;

	@PostMapping
	public Film addNewFilm(@Valid @RequestBody Film film) {
		log.info("Endpoint -> Create film");
		checkingFilmForValid(film);
		return filmService.addNewFilm(film);
	}

	@PutMapping
	public Film updateFilm(@Valid @RequestBody Film film) {
		log.info("Endpoint -> Update film");
		checkingFilmForValid(film);
		return filmService.updateFilm(film);
	}

	@GetMapping ("/{id}")
	public Film getFilmById(@PathVariable (required = false) Long id) {
		log.info("Endpoint -> Get film {}", id);
		return filmService.getFilmById(id);
	}

	@GetMapping
	public Collection<Film> getAllFilms() {
		log.info("Endpoint -> Get films");
		return filmService.getAllFilms();
	}

	@PutMapping ("/{id}/like/{userId}")
	public void addLike(@PathVariable Long id, @PathVariable Long userId) {
		log.info("Endpoint -> Update film {}, liked user {}", id, userId);
		filmService.addLike(id, userId);
	}

	@DeleteMapping ("/{id}/like/{userId}")
	public void deleteLike(@PathVariable (required = false) Long id, @PathVariable (required = false) Long userId) {
		log.info("Endpoint -> Delete in film {}, like user {}", id, userId);
		filmService.deleteLike(id, userId);
	}

	@GetMapping ("/popular")
	public Collection<Film> getTopRatingFilms(@RequestParam (defaultValue = "10", required = false) Integer count) {
		log.info("Endpoint ->  Get rating films, count {}", count);
		return filmService.getTopRatingFilms(count);
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
