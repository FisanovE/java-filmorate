package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
	private User user;
	UserController controller;

	@BeforeEach
	void init() {
		controller = new UserController();
	}

	@Test
	@DisplayName ("Добавление нового пользователя")
	void shouldAddNewUser() throws ValidationException {
		User userNew = createUser();
		controller.addNewUser(userNew);
		List<User> list  = new ArrayList<>(controller.getAllUsers());

		assertFalse(list.isEmpty(), "User is not added");
	}

	@Test
	@DisplayName ("Валидация: Email без @")
	void shouldReturnExceptionWhenEmailNewUserIsNotValid() {
		User userNew = createUser();
		String notValidEmail = "mailmail.ru";
		userNew.setEmail(notValidEmail);
		List<User> list  = new ArrayList<>(controller.getAllUsers());
		final ValidationException exception = assertThrows(
				ValidationException.class, () -> controller.addNewUser(userNew));
		assertAll(() -> assertEquals("Invalid e-mail format: \"" + notValidEmail + "\"", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Валидация: Email пуст")
	void shouldReturnExceptionWhenEmailNewUserIsNotValid2() {
		User userNew = createUser();
		String notValidEmail = "";
		userNew.setEmail(notValidEmail);
		List<User> list  = new ArrayList<>(controller.getAllUsers());
		final ValidationException exception = assertThrows(
				ValidationException.class, () -> controller.addNewUser(userNew));
		assertAll(() -> assertEquals("Invalid e-mail format: \"" + notValidEmail + "\"", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Валидация: Login пуст")
	void shouldReturnExceptionWhenLoginNewUserIsNotValid() {
		User userNew = createUser();
		String notValidLogin = "";
		userNew.setLogin(notValidLogin);
		List<User> list  = new ArrayList<>(controller.getAllUsers());
		final ValidationException exception = assertThrows(
				ValidationException.class, () -> controller.addNewUser(userNew));
		assertAll(() -> assertEquals("Wrong login: \"" + notValidLogin + "\"", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Валидация: Login содержит пробелы")
	void shouldReturnExceptionWhenLoginNewUserIsNotValid2() {
		User userNew = createUser();
		String notValidLogin = " ";
		userNew.setLogin(notValidLogin);
		List<User> list  = new ArrayList<>(controller.getAllUsers());
		final ValidationException exception = assertThrows(
				ValidationException.class, () -> controller.addNewUser(userNew));
		assertAll(() -> assertEquals("Wrong login: \"" + notValidLogin + "\"", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Name пуст")
	void shouldReturnExceptionWhenNameNewUserIsEmpty() throws ValidationException {
		User userNew = createUser();
		String name = "";
		userNew.setName(name);
		controller.addNewUser(userNew);
		List<User> list  = new ArrayList<>(controller.getAllUsers());
		assertAll(
				() -> assertFalse(list.isEmpty(), "User is not added"),
				() -> assertEquals(userNew.getLogin(), list.get(0).getName(), "Logins are not equal"));
	}

	@Test
	@DisplayName ("Валидация: Дата рождения в будущем")
	void shouldReturnExceptionWhenBirthdayNewUserIsNotValid() {
		User userNew = createUser();
		String notValidBirthday = "2027-08-20";
		userNew.setBirthday(LocalDate.parse(notValidBirthday, DateUtils.formatter));
		List<User> list  = new ArrayList<>(controller.getAllUsers());
		final ValidationException exception = assertThrows(
				ValidationException.class, () -> controller.addNewUser(userNew));
		assertAll(() -> assertEquals("Date of birth cannot be in the future: \"" + notValidBirthday + "\"", exception.getMessage()),
				() -> assertTrue(list.isEmpty()));
	}

	@Test
	@DisplayName ("Обновление пользователя ")
	void shouldUpdateUser() throws ValidationException {
		User userNew = createUser();
		User userAdded = updateUser();
		controller.addNewUser(userNew);
		controller.updateUser(userAdded);
		List<User> list  = new ArrayList<>(controller.getAllUsers());

		assertAll(
				() -> assertFalse(list.isEmpty(), "User is not added"),
				() -> assertEquals(userAdded, list.get(0), "Users are not equal"));
	}

	@Test
	@DisplayName ("Получение списка пользователей")
	void shouldReturnListUsers() throws ValidationException {
		User userNew = createUser();
		controller.addNewUser(userNew);
		List<User> list  = new ArrayList<>(controller.getAllUsers());

		assertFalse(list.isEmpty(), "The list is empty");
	}

	private User createUser() {
		user = User.builder()
				   .email("mail@mail.ru")
				   .login("Login")
				   .name("Name")
				   .birthday(LocalDate.of(2022, 8, 20))
				   .build();
		return user;
	}

	private User updateUser() {
		user = User.builder()
				   .id(1)
				   .email("mail@yandex.ru")
				   .login("LoginUpdate")
				   .name("NameUpdate")
				   .birthday(LocalDate.of(1946, 8, 20))
				   .build();
		return user;
	}
}