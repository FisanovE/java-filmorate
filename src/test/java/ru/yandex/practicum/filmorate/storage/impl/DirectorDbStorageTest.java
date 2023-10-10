package ru.yandex.practicum.filmorate.storage.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.controller.DirectorController;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DirectorDbStorageTest {
	private Director director;
	DirectorController controller;
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void init() {
		controller = new DirectorController(new DirectorService(new DirectorDbStorage(jdbcTemplate)));
	}

	@Test
	@DisplayName ("Добавление нового режиссёра")
	void shouldAddNewDirector() {
		Director directorNew = createDirector();
		controller.addNewDirector(directorNew);
		List<Director> list = new ArrayList<>(controller.getAllDirectors());

		assertFalse(list.isEmpty(), "Director not added");
	}

	@Test
	void updateDirector() {
	}

	@Test
	void getAllDirectors() {
	}

	@Test
	void getDirectorById() {
	}

	@Test
	void deleteDirectorById() {
	}

	private Director createDirector() {
		return Director.builder()
				   .name("Name Director")
				   .build();
	}
}