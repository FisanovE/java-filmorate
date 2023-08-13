package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import ru.yandex.practicum.filmorate.utils.DateUtils;

import javax.validation.Valid;

import java.time.LocalDate;
import java.util.*;


@Controller
@Slf4j
@RestController
@RequestMapping ("/films")
public class FilmController {

	private final Map<Integer, Film> films = new HashMap<>();
	private int counter = 1;


	@PostMapping
	public Film addNewFilm(@Valid @RequestBody Film film) throws ValidationException {
		log.info("Получен запрос к эндпоинту: POST /films");
		if (film.getName().isBlank()) {
			throw new ValidationException("Не верный формат названия: \"" + film.getName() + "\"");
		}
		if (film.getDescription().length() > 200) {
			throw new ValidationException("Максимальная длина описания — 200 символов, у вас:  \"" + film.getDescription()
																										 .length() + "\" символов");
		}
		if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28", DateUtils.formatter))) {
			throw new ValidationException("Дата релиза фильма не должна быть раньше 1895.12.28, у вас: \"" + film.getReleaseDate() + "\"");
		}
		if (film.getDuration() < 0) {
			throw new ValidationException("Продолжительность фильма должна быть положительной, у вас:  \"" + film.getDuration());
		}
		film.setId(counter);
		counter++;
		films.put(film.getId(), film);
		log.info("Добавлен фильм {}.", film);
		return films.get(film.getId());
	}


	@PutMapping
	public Film updateFilm(@Valid @RequestBody Film film) throws ValidationException {
		log.info("Получен запрос к эндпоинту: PUT /films");
		if (film.getName().isBlank()) {
			throw new ValidationException("Не верный логин: \"" + film.getName() + "\"");
		}
		if (film.getDescription().length() > 200) {
			throw new ValidationException("Максимальная длина описания — 200 символов, у вас:  \"" + film.getDescription()
																										 .length() + "\" символов");
		}
		if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28", DateUtils.formatter))) {
			throw new ValidationException("Дата релиза фильма не должна быть раньше 1895.12.28, у вас: \"" + film.getReleaseDate() + "\"");
		}
		if (film.getDuration() < 0) {
			throw new ValidationException("Продолжительность фильма должна быть положительной, у вас:  \"" + film.getDuration());
		}
		for (Film currentFilm : films.values()) {
			if (currentFilm.getName().equals(film.getName()) && currentFilm.getReleaseDate()
																		   .equals(film.getReleaseDate())) {
				throw new ValidationException("Данная информация по фильму " + film.getName() + " уже имеется.");
			}
			films.put(currentFilm.getId(), film);
		}
		log.info("Изменён фильм {}.", film);
		return films.get(film.getId());
	}


	@GetMapping
	public Collection<Film> getAllFilms() {
		return films.values();
	}

}
