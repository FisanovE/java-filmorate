package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.utils.DateUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@RestController
@Component
@RequiredArgsConstructor
public class FilmController {

	private final FilmService filmService;

	@PostMapping ("/films")
	public Film addNewFilm(@RequestBody Film film) {
		log.info("Endpoint -> Create film");
		checkingFilmForValid(film);
		return filmService.addNewFilm(film);
	}

	@PutMapping ("/films")
	public Film updateFilm(@RequestBody Film film) {
		log.info("Endpoint -> Update film");
		checkingFilmForValid(film);
		return filmService.updateFilm(film);
	}

	@GetMapping ("/films/{id}")
	public Film getFilmById(@PathVariable (required = false) Long id) {
		log.info("Endpoint -> Get film {}", id);
		return filmService.getFilmById(id);
	}

	@GetMapping ("/films")
	public Collection<Film> getAllFilms() {
		log.info("Endpoint -> Get films");
		return filmService.getAllFilms();
	}

	@PutMapping ("/films/{id}/like/{userId}")
	public void addLike(@PathVariable Long id, @PathVariable Long userId) {
		log.info("Endpoint -> Update film {}, liked user {}", id, userId);
		filmService.addLike(id, userId);
	}

	@DeleteMapping ("/films/{id}/like/{userId}")
	public void deleteLike(@PathVariable (required = false) Long id, @PathVariable (required = false) Long userId) {
		log.info("Endpoint -> Delete in film {}, like user {}", id, userId);
		filmService.deleteLike(id, userId);
	}

	@GetMapping ("/films/popular")
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

	@GetMapping ("/genres")
	public Collection<Genre> getAllGenres() {
		log.info("Endpoint -> Get genres");
		return filmService.getAllGenres();
	}

	@GetMapping ("/genres/{id}")
	public Genre getGenresById(@PathVariable (required = false) Long id) {
		log.info("Endpoint -> Get genres id {}", id);
		return filmService.getGenresById(id);
	}

	@GetMapping ("/mpa")
	public Collection<Mpa> getAllRatingsMpa() {
		log.info("Endpoint -> Get mpa");
		return filmService.getAllRatingsMpa();
	}

	@GetMapping ("/mpa/{id}")
	public Mpa getRatingsMpaById(@PathVariable (required = false) Long id) {
		log.info("Endpoint -> Get mpa id {}", id);
		return filmService.getRatingsMpaById(id);
	}

	/**
	 * ALG_7
	 */
	@GetMapping ("/films/director/{directorId}")
	public Collection<Film> getAllFilmsByDirector(@PathVariable Long directorId, @RequestParam String sortBy) {
		log.info("ALG_7. Endpoint ->  Get films/directorId {} sortBy {} ", directorId, sortBy);
		if (Objects.equals(sortBy, "year") || Objects.equals(sortBy, "likes")) {
			return filmService.getAllFilmsByDirector(directorId, sortBy);
		} else {
			throw new NotFoundException("ALG_7. Invalid RequestParam:  " + sortBy);
		}
	}

	/**
	 * ALG_2
	 */
	@GetMapping ("/films/search")
	public Collection<Film> searchFilms(@RequestParam String query, @RequestParam String by) {
		log.info("ALG_2. Endpoint ->  Get films/search {} by {} ", query, by);
		if (Objects.equals(by, "director") || Objects.equals(by, "title") || Objects.equals(by, "title,director") ||
				Objects.equals(by, "director,title")) {
			return filmService.searchFilms(query, by);
		} else {
			throw new NotFoundException("ALG_2. Invalid search param:  " + by);
		}
	}

	/** ALG_6 */
 	 @DeleteMapping("/films/{id}")
	 public void deleteFilmById(@PathVariable Long id) {
        filmService.deleteFilm(id);
    }
	/**
	 * ALG_3
	 */
	@GetMapping ("/films/common")
	public Collection<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
		log.info("ALG_3. Endpoint ->  Get films/common userId {} friendId {} ", userId, friendId);
		return filmService.getCommonFilms(userId, friendId);
	}

}
