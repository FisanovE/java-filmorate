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
		controller = new FilmController();
	}

	@Test
	@DisplayName ("Добавление нового фильма")
	void shouldAddNewFilmWhenEmailIsValid() throws ValidationException {
		Film filmNew = createFilm();
		System.out.println("filmNew: " + filmNew.toString());
		controller.addNewFilm(filmNew);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());

		assertFalse(list.isEmpty(), "Film not added");
	}

	@Test
	@DisplayName ("Валидация: Name пуст")
	void shouldReturnExceptionWhenNameNewFilmIsEmpty() {
		Film filmNew = createFilm();
		String notValidName = "";
		filmNew.setName(notValidName);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());
		final ValidationException exception = assertThrows(
				ValidationException.class, () -> controller.addNewFilm(filmNew));
		assertAll(() -> assertEquals("Invalid title format: \"" + notValidName + "\"", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Валидация: Description больше 200 символов")
	void shouldReturnExceptionWhenDescriptionNewFilmMoreThan200Char() {
		Film filmNew = createFilm();
		String notValidDescription = "This is a pretty famous film that no one has ever seen. That's exactly what he's " +
				"famous for. Critics do not respond to him in any way, because they have not seen him once. The audience " +
				"is enthusiastically silent about him for the same reason.";
		filmNew.setDescription(notValidDescription);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());
		final ValidationException exception = assertThrows(
				ValidationException.class, () -> controller.addNewFilm(filmNew));
		assertAll(() -> assertEquals("The maximum description length is 200 characters, you have: \"" +
						notValidDescription.length() + "\" characters", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Валидация: releaseDate раньше 1895.12.28")
	void shouldReturnExceptionWhenReleaseDateNewFilmIsNotValid() {
		Film filmNew = createFilm();
		String notValidReleaseDate = "1795-12-28";
		filmNew.setReleaseDate(LocalDate.parse(notValidReleaseDate, DateUtils.formatter));
		List<Film> list  = new ArrayList<>(controller.getAllFilms());
		final ValidationException exception = assertThrows(
				ValidationException.class, () -> controller.addNewFilm(filmNew));
		assertAll(() -> assertEquals("Movie release date should not be earlier than 1895.12.28, you have: \"" +
						notValidReleaseDate + "\"", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Валидация: duration отрицательная")
	void shouldReturnExceptionWhenDurationNewFilmIsNotValid() {
		Film filmNew = createFilm();
		int notValidDuration = -1;
		filmNew.setDuration(notValidDuration);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());
		final ValidationException exception = assertThrows(
				ValidationException.class, () -> controller.addNewFilm(filmNew));
		assertAll(() -> assertEquals("The duration of the film should be positive, you have:  \"" + notValidDuration, exception.getMessage()),
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
				() -> assertFalse(list.isEmpty(), "Film not added"),
				() -> assertEquals(filmAdded, list.get(0), "Films are not equal"));
	}

	@Test
	@DisplayName ("Получение списка пользователей")
	void shouldReturnListFilms() throws ValidationException {
		Film filmNew = createFilm();
		controller.addNewFilm(filmNew);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());

		assertFalse(list.isEmpty(), "The list is empty");
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