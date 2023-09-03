package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
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
		log.info("Received request to endpoint: POST /films");
		return filmService.addNewFilm(film);
	}

	@PutMapping
	public Film updateFilm(@Valid @RequestBody Film film) {
		log.info("Received request to endpoint: PUT /films");
		return filmService.updateFilm(film);
	}

	@GetMapping ("/{id}")
	public Film getFilmById(@PathVariable (required = false) Long id) {
		log.info("Received request to endpoint: GET /films/{}", id);
		return filmService.getFilmById(id);
	}

	@GetMapping
	public Collection<Film> getAllFilms() {
		log.info("Received request to endpoint: GET /films");
		return filmService.getAllFilms();
	}

	@PutMapping ("/{id}/like/{userId}")
	public void addLike(@PathVariable Long id, @PathVariable Long userId) {
		log.info("Received request to endpoint: PUT /films/{}/like/{}", id, userId);
		filmService.addLike(id, userId);
	}

	@DeleteMapping ("/{id}/like/{userId}")
	public void deleteLike(@PathVariable (required = false) Long id, @PathVariable (required = false) Long userId) {
		log.info("Received request to endpoint: DELETE /films/{}/like/{}", id, userId);
		filmService.deleteLike(id, userId);
	}

	@GetMapping ("/popular")
	public Collection<Film> getTopRatingFilms(@RequestParam (defaultValue = "10", required = false) Integer count) {
		log.info("Received request to endpoint: GET /films/popular?count={}", count);
		return filmService.getTopRatingFilms(count);
	}
}
