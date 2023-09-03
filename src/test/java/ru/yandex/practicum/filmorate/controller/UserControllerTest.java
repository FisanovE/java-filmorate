package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
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
		controller = new UserController(new UserService(new InMemoryUserStorage()));
	}

	@Test
	@DisplayName ("Добавление нового пользователя")
	void shouldAddNewUser() throws ValidationException {
		User userNew = createUser();
		controller.addNewUser(userNew);
		List<User> list = new ArrayList<>(controller.getAllUsers());

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
		List<User> list = new ArrayList<>(controller.getAllUsers());
		assertAll(() -> assertFalse(list.isEmpty(), "User is not added"), () -> assertEquals(userNew.getLogin(), list.get(0)
																													 .getName(), "Logins are not equal"));
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
		List<User> list = new ArrayList<>(controller.getAllUsers());

		assertAll(() -> assertFalse(list.isEmpty(), "User is not added"), () -> assertEquals(userAdded, list.get(0), "Users are not equal"));
	}

	@Test
	@DisplayName ("Получение списка пользователей")
	void shouldReturnListUsers() {
		User userNew = createUser();
		controller.addNewUser(userNew);
		List<User> list = new ArrayList<>(controller.getAllUsers());

		assertFalse(list.isEmpty(), "The list is empty");
	}

	@Test
	@DisplayName ("Добавление друга")
	void shouldAddFriend() throws ValidationException {
		User userNew1 = createUser();
		User userNew2 = createUser();
		userNew2.setEmail("mail@yandex.ru");
		userNew2.setName("NameUpdate");
		userNew2.setLogin("LoginUpdate");
		User userAded1 = controller.addNewUser(userNew1);
		User userAded2 = controller.addNewUser(userNew2);

		controller.addFriend(userAded1.getId(), userAded2.getId());

		List<User> list1 = new ArrayList<>(controller.getAllFriendsOfUser(userAded1.getId()));
		List<User> list2 = new ArrayList<>(controller.getAllFriendsOfUser(userAded2.getId()));

		assertAll(
				() -> assertFalse(list1.isEmpty(), "Friend of User1 is not added"),
				() -> assertFalse(list2.isEmpty(), "Friend of User2 is not added"),
				() -> assertEquals(userAded2, list1.get(0), "Users are not equal"),
				() -> assertEquals(userAded1, list2.get(0), "Users are not equal"));
	}

	@Test
	@DisplayName ("Удаление друга")
	void shouldDeleteFriend() throws ValidationException {
		User userNew1 = createUser();
		User userNew2 = createUser();
		userNew2.setEmail("mail@yandex.ru");
		userNew2.setName("NameUpdate");
		userNew2.setLogin("LoginUpdate");
		User userAded1 = controller.addNewUser(userNew1);
		User userAded2 = controller.addNewUser(userNew2);
		controller.addFriend(userAded1.getId(), userAded2.getId());

		controller.deleteFriend(userAded1.getId(), userAded2.getId());

		List<User> list1 = new ArrayList<>(controller.getAllFriendsOfUser(userAded1.getId()));
		List<User> list2 = new ArrayList<>(controller.getAllFriendsOfUser(userAded2.getId()));

		assertAll(
				() -> assertTrue(list1.isEmpty(), "User 1's friend list must be empty"),
				() -> assertTrue(list2.isEmpty(), "User 2's friend list must be empty"));
	}

	@Test
	@DisplayName ("Получение списка друзей пользователя")
	void shouldAllFriendsByUser() throws ValidationException {
		User userNew1 = createUser();
		User userNew2 = createUser();
		User userNew3 = createUser();
		userNew2.setEmail("mail@yandex.ru");
		userNew2.setName("NameUpdate");
		userNew2.setLogin("LoginUpdate");
		userNew3.setEmail("mail3@yandex.ru");
		userNew3.setName("NameUpdate3");
		userNew3.setLogin("LoginUpdate3");
		User userAded1 = controller.addNewUser(userNew1);
		User userAded2 = controller.addNewUser(userNew2);
		User userAded3 = controller.addNewUser(userNew3);
		controller.addFriend(userAded1.getId(), userAded2.getId());
		controller.addFriend(userAded1.getId(), userAded3.getId());

		List<User> list = new ArrayList<>(controller.getAllFriendsOfUser(userAded1.getId()));

		assertAll(
				() -> assertFalse(list.isEmpty(), "User's friend list must be not empty"),
				() -> assertEquals(list.size(), 2, "List size not equals 2"),
				() -> assertEquals(userAded2, list.get(0), "Users are not equal"),
				() -> assertEquals(userAded3, list.get(1), "Users are not equal"));
	}

	@Test
	@DisplayName ("Получение списка общих друзей пользователей")
	void shouldMutualFriends() throws ValidationException {
		User userNew1 = createUser();
		User userNew2 = createUser();
		User userNew3 = createUser();
		userNew2.setEmail("mail@yandex.ru");
		userNew2.setName("NameUpdate");
		userNew2.setLogin("LoginUpdate");
		userNew3.setEmail("mail3@yandex.ru");
		userNew3.setName("NameUpdate3");
		userNew3.setLogin("LoginUpdate3");
		User userAded1 = controller.addNewUser(userNew1);
		User userAded2 = controller.addNewUser(userNew2);
		User userAded3 = controller.addNewUser(userNew3);
		controller.addFriend(userAded1.getId(), userAded3.getId());
		controller.addFriend(userAded2.getId(), userAded3.getId());

		List<User> list = new ArrayList<>(controller.getMutualFriends(userAded1.getId(), userAded2.getId()));

		assertAll(
				() -> assertFalse(list.isEmpty(), "User's friend list must be not empty"),
				() -> assertEquals(list.size(), 1, "List size not equals 1"),
				() -> assertEquals(userAded3, list.get(0), "Users are not equal"));
	}

	private User createUser() {
		user = User.builder().email("mail@mail.ru").login("Login").name("Name").birthday(LocalDate.of(2022, 8, 20))
				   .build();
		return user;
	}

	private User updateUser() {
		user = User.builder().id(1L).email("mail@yandex.ru").login("LoginUpdate").name("NameUpdate")
				   .birthday(LocalDate.of(1946, 8, 20)).build();
		return user;
	}
}