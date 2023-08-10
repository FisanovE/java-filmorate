package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;

import java.util.*;


@Controller
@Slf4j
@RestController
@RequestMapping ("/films")
public class FilmController {

	private final Map<Integer, Film> films = new HashMap<>();
	private int counter = 1;


	@PostMapping
	public Film addNewFilm(@Valid @RequestBody Film film) {
		log.info("Получен запрос к эндпоинту: POST /films");
		film.setId(counter);
		counter++;
		films.put(film.getId(), film);
		return film;
	}


	@PutMapping
	public Film updateFilm(@Valid @RequestBody Film film) throws ValidationException {
		log.info("Получен запрос к эндпоинту: PUT /films");
		for (Film currentFilm : films.values()) {
			if (currentFilm.getName().equals(film.getName()) && currentFilm.getReleaseDate()
																		   .equals(film.getReleaseDate())) {
				throw new ValidationException("Данная информация по фильму " + film.getName() + " уже имеется.");
			}
			films.put(currentFilm.getId(), film);
		}
		return film;
	}


	@GetMapping
	public Collection<Film> getAllFilms() {
		return films.values();
	}

}
