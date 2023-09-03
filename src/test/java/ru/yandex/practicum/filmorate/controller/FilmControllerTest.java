package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.utils.DateUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
	private Film film;
	private FilmController controller;
	private Validator validator;
	private Set<ConstraintViolation<Film>> violations;

	@BeforeEach
	void init() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
		controller = new FilmController(new FilmService(new InMemoryFilmStorage()));
	}

	@Test
	@DisplayName ("Добавление нового фильма")
	void shouldAddNewFilmWhenEmailIsValid() {
		Film filmNew = createFilm();
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
		violations = validator.validate(filmNew);
		assertFalse(violations.isEmpty(), violations.toString());
		violations.clear();
	}

	@Test
	@DisplayName ("Валидация: Description больше 200 символов")
	void shouldReturnExceptionWhenDescriptionNewFilmMoreThan200Char() {
		Film filmNew = createFilm();
		String notValidDescription = "This is a pretty famous film that no one has ever seen. That's exactly what he's " +
				"famous for. Critics do not respond to him in any way, because they have not seen him once. The audience " +
				"is enthusiastically silent about him for the same reason.";
		filmNew.setDescription(notValidDescription);
		violations = validator.validate(filmNew);
		assertFalse(violations.isEmpty(), violations.toString());
		violations.clear();
	}

	@Test
	@DisplayName ("Валидация: releaseDate раньше 1895.12.28")
	void shouldReturnExceptionWhenReleaseDateNewFilmIsNotValid2() {

		Film filmNew = createFilm();
		String notValidReleaseDate = "1795-12-28";
		filmNew.setReleaseDate(LocalDate.parse(notValidReleaseDate, DateUtils.formatter));
		violations = validator.validate(filmNew);
		assertFalse(violations.isEmpty(), violations.toString());
		violations.clear();
	}

	@Test
	@DisplayName ("Валидация: duration отрицательная")
	void shouldReturnExceptionWhenDurationNewFilmIsNotValid() {
		Film filmNew = createFilm();
		int notValidDuration = -1;
		filmNew.setDuration(notValidDuration);
		violations = validator.validate(filmNew);
		assertFalse(violations.isEmpty(), violations.toString());
		violations.clear();
	}

	@Test
	@DisplayName ("Обновление фильма ")
	void shouldUpdateFilm() {
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
	void shouldReturnListFilms() {
		Film filmNew = createFilm();
		controller.addNewFilm(filmNew);
		List<Film> list  = new ArrayList<>(controller.getAllFilms());

		assertFalse(list.isEmpty(), "The list is empty");
	}

	@Test
	@DisplayName ("Добавление лайка")
	void shouldAddLike() {
		Film filmNew = createFilm();
		Film filmAdded = controller.addNewFilm(filmNew);

		controller.addLike(filmAdded.getId(), 2L);

		List<Film> films  = new ArrayList<>(controller.getTopRatingFilms(10));
		List<Long> likes  = new ArrayList<>(films.get(0).getLikes());

		assertAll(
				() -> assertFalse(likes.isEmpty(), "Like not added"),
				() -> assertEquals(2, likes.get(0), "Films id are not equal"));
	}

	@Test
	@DisplayName ("Удаление лайка")
	void shouldDeleteLike() {
		Film filmNew = createFilm();
		Film filmAdded = controller.addNewFilm(filmNew);
		controller.addLike(filmAdded.getId(), 2L);

		controller.deleteLike(filmAdded.getId(), 2L);

		List<Film> films  = new ArrayList<>(controller.getTopRatingFilms(10));
		List<Long> likes  = new ArrayList<>(films.get(0).getLikes());

		assertTrue(likes.isEmpty(), "Likes list must by empty");
	}

	@Test
	@DisplayName ("Получение списка лучших фильмов")
	void shouldReturnTopRatingFilms() {
		Film filmNew1 = createFilm();
		Film filmNew2 = createFilm();
		filmNew2.setName("Name Film2");
		Film filmAdded1 = controller.addNewFilm(filmNew1);
		Film filmAdded2 = controller.addNewFilm(filmNew2);
		controller.addLike(filmAdded1.getId(), 2L);
		controller.addLike(filmAdded1.getId(), 3L);
		controller.addLike(filmAdded2.getId(), 2L);

		List<Film> films  = new ArrayList<>(controller.getTopRatingFilms(10));

		assertAll(
				() -> assertFalse(films.isEmpty(), "Rating list must by not empty"),
				() -> assertEquals(filmAdded2.getId(), films.get(0).getId(), "Films id are not equal"),
				() -> assertEquals(2, films.size(), "List`s size not equal 2"));
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
				   .id(1L)
				   .name("updateName Film")
				   .description("blah-blah-blah")
				   .releaseDate(LocalDate.of(2022, 5, 12))
				   .duration(60)
				   .build();
		return film;
	}
}