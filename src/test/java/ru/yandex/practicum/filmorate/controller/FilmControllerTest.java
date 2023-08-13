package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utils.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
	private Film film;
	FilmController controller;

	@BeforeEach
	void init() {
		/*String[] args = new String[0];
		SpringApplication.run(FilmorateApplication.class, args);*/
		controller = new FilmController();
	}

	@Test
	@DisplayName ("Добавление нового фильма")
	void shouldAddNewFilmWhenEmailIsValid() throws ValidationException {
		Film FilmNew = createFilm();
		System.out.println("FilmNew: " + FilmNew.toString());
		controller.addNewFilm(FilmNew);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());

		assertFalse(list.isEmpty(), "Фильм не добавляется");
	}

	@Test
	@DisplayName ("Валидация: Name пуст")
	void shouldReturnExceptionWhenNameNewFilmIsEmpty() {
		Film FilmNew = createFilm();
		String notValidName = "";
		FilmNew.setName(notValidName);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());
		final ValidationException exception = assertThrows(
				ValidationException.class,
				new Executable() {
					@Override
					public void execute() throws ValidationException {controller.addNewFilm(FilmNew);}
				});
		assertAll(() -> assertEquals("Не верный формат названия: \"" + notValidName + "\"", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Валидация: Description больше 200 символов")
	void shouldReturnExceptionWhenDescriptionNewFilmMoreThan200Char() {
		Film FilmNew = createFilm();
		String notValidDescription = "This is a pretty famous film that no one has ever seen. That's exactly what he's " +
				"famous for. Critics do not respond to him in any way, because they have not seen him once. The audience " +
				"is enthusiastically silent about him for the same reason.";
		FilmNew.setDescription(notValidDescription);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());
		final ValidationException exception = assertThrows(
				ValidationException.class,
				new Executable() {
					@Override
					public void execute() throws ValidationException {controller.addNewFilm(FilmNew);}
				});
		assertAll(() -> assertEquals("Максимальная длина описания — 200 символов, у вас:  \"" +
						notValidDescription.length() + "\" символов", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Валидация: releaseDate раньше 1895.12.28")
	void shouldReturnExceptionWhenReleaseDateNewFilmIsNotValid() {
		Film FilmNew = createFilm();
		String notValidReleaseDate = "1795-12-28";
		FilmNew.setReleaseDate(LocalDate.parse(notValidReleaseDate, DateUtils.formatter));
		List<Film> list  = new ArrayList<>(controller.getAllFilms());
		final ValidationException exception = assertThrows(
				ValidationException.class,
				new Executable() {
					@Override
					public void execute() throws ValidationException {controller.addNewFilm(FilmNew);}
				});
		assertAll(() -> assertEquals("Дата релиза фильма не должна быть раньше 1895.12.28, у вас: \"" +
						notValidReleaseDate + "\"", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Валидация: duration отрицательная")
	void shouldReturnExceptionWhenDurationNewFilmIsNotValid() {
		Film FilmNew = createFilm();
		int notValidDuration = -1;
		FilmNew.setDuration(notValidDuration);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());
		final ValidationException exception = assertThrows(
				ValidationException.class,
				new Executable() {
					@Override
					public void execute() throws ValidationException {controller.addNewFilm(FilmNew);}
				});
		assertAll(() -> assertEquals("Продолжительность фильма должна быть положительной, у вас:  \"" + notValidDuration, exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Обновление фильма ")
	void shouldUpdateFilm() throws ValidationException {
		Film filmNew = createFilm();
		Film filmAdded = updateFilm();
		controller.addNewFilm(filmNew);
		controller.updateFilm(filmAdded);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());

		assertAll(
				() -> assertFalse(list.isEmpty(), "Фильм не добавляется"),
				() -> assertEquals(filmAdded, list.get(0), "Фильмы не совпадают"));
	}

	@Test
	@DisplayName ("Получение списка пользователей")
	void shouldReturnListFilms() throws ValidationException {
		Film FilmNew = createFilm();
		controller.addNewFilm(FilmNew);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());

		assertFalse(list.isEmpty(), "Список пуст");
	}

	private Film createFilm() {
		film = film.builder()
				   .name("Name Film")
				   .description("blah-blah-blah")
				   .releaseDate(LocalDate.of(2022, 8, 20))
				   .duration(120)
				   .build();
		return film;
	}

	private Film updateFilm() {
		film = film.builder()
				   .id(1)
				   .name("updateName Film")
				   .description("blah-blah-blah")
				   .releaseDate(LocalDate.of(2022, 5, 12))
				   .duration(60)
				   .build();
		return film;
	}
}