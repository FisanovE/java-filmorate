package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
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

class UserControllerTest {
	private User user;
	private UserController controller;
	private Validator validator;
	private Set<ConstraintViolation<User>> violations;

	@BeforeEach
	void init() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
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
		violations = validator.validate(userNew);
		assertFalse(violations.isEmpty(), violations.toString());
		violations.clear();
	}

	@Test
	@DisplayName ("Валидация: Email пуст")
	void shouldReturnExceptionWhenEmailNewUserIsNotValid2() {
		User userNew = createUser();
		String notValidEmail = "";
		userNew.setEmail(notValidEmail);
		violations = validator.validate(userNew);
		assertFalse(violations.isEmpty(), violations.toString());
		violations.clear();
	}

	@Test
	@DisplayName ("Валидация: Login пуст")
	void shouldReturnExceptionWhenLoginNewUserIsNotValid() {
		User userNew = createUser();
		String notValidLogin = "";
		userNew.setLogin(notValidLogin);
		violations = validator.validate(userNew);
		assertFalse(violations.isEmpty(), violations.toString());
		violations.clear();
	}

	@Test
	@DisplayName ("Валидация: Login содержит пробелы")
	void shouldReturnExceptionWhenLoginNewUserIsNotValid2() {
		User userNew = createUser();
		String notValidLogin = " ";
		userNew.setLogin(notValidLogin);
		violations = validator.validate(userNew);
		assertFalse(violations.isEmpty(), violations.toString());
		violations.clear();
	}

	@Test
	@DisplayName ("Name пуст")
	void shouldReturnExceptionWhenNameNewUserIsEmpty() {
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
		violations = validator.validate(userNew);
		assertFalse(violations.isEmpty(), violations.toString());
		violations.clear();
	}

	@Test
	@DisplayName ("Обновление пользователя ")
	void shouldUpdateUser() {
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
	void shouldReturnListUsers() {
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