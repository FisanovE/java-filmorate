package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.DateUtils;
import ru.yandex.practicum.filmorate.utils.GsonUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
	Gson gson = GsonUtils.getGson();
	private User user;
	UserController controller;

	@BeforeEach
	void init() {
		/*String[] args = new String[0];
		SpringApplication.run(FilmorateApplication.class, args);*/
		controller = new UserController();
	}

	@Test
	@DisplayName ("Добавление нового пользователя")
	void shouldAddNewUser() throws ValidationException {
		User userNew = createUser();
		controller.addNewUser(userNew);
		List<User> list  = new ArrayList<>(controller.getAllUsers());

		assertFalse(list.isEmpty(), "Пользователь не добавляется");
	}

	@Test
	@DisplayName ("Валидация: Email без @")
	void shouldReturnExceptionWhenEmailNewUserIsNotValid() {
		User userNew = createUser();
		String notValidEmail = "mailmail.ru";
		userNew.setEmail(notValidEmail);
		List<User> list  = new ArrayList<>(controller.getAllUsers());
		final ValidationException exception = assertThrows(
				ValidationException.class,
				new Executable() {
					@Override
					public void execute() throws ValidationException {
						controller.addNewUser(userNew);
					}
				});
		assertAll(() -> assertEquals("Не верный формат электронной почты: \"" + notValidEmail + "\"", exception.getMessage()),
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
				ValidationException.class,
				new Executable() {
					@Override
					public void execute() throws ValidationException {
						controller.addNewUser(userNew);
					}
				});
		assertAll(() -> assertEquals("Не верный формат электронной почты: \"" + notValidEmail + "\"", exception.getMessage()),
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
				ValidationException.class,
				new Executable() {
					@Override
					public void execute() throws ValidationException {
						controller.addNewUser(userNew);
					}
				});
		assertAll(() -> assertEquals("Не верный логин: \"" + notValidLogin + "\"", exception.getMessage()),
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
				ValidationException.class,
				new Executable() {
					@Override
					public void execute() throws ValidationException {
						controller.addNewUser(userNew);
					}
				});
		assertAll(() -> assertEquals("Не верный логин: \"" + notValidLogin + "\"", exception.getMessage()),
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
				() -> assertFalse(list.isEmpty(), "Пользователь не добавляется"),
				() -> assertEquals(userNew.getLogin(), list.get(0).getName(), "Логины не совпадают"));
	}

	@Test
	@DisplayName ("Валидация: Дата рождения в будущем")
	void shouldReturnExceptionWhenBirthdayNewUserIsNotValid() {
		User userNew = createUser();
		String notValidBirthday = "2027-08-20";
		userNew.setBirthday(LocalDate.parse(notValidBirthday, DateUtils.formatter));
		List<User> list  = new ArrayList<>(controller.getAllUsers());
		final ValidationException exception = assertThrows(
				ValidationException.class,
				new Executable() {
					@Override
					public void execute() throws ValidationException {
						controller.addNewUser(userNew);
					}
				});
		assertAll(() -> assertEquals("Дата рождения не может быть в будущем: \"" + notValidBirthday + "\"", exception.getMessage()),
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
				() -> assertFalse(list.isEmpty(), "Пользователь не добавляется"),
				() -> assertEquals(userAdded, list.get(0), "Пользователи не совпадают"));
	}

	@Test
	@DisplayName ("Получение списка пользователей")
	void shouldReturnListUsers() throws ValidationException {
		User userNew = createUser();
		controller.addNewUser(userNew);
		List<User> list  = new ArrayList<>(controller.getAllUsers());

		assertFalse(list.isEmpty(), "Список пуст");
	}

/*	@Test
	@DisplayName ("Добавление нового пользователя ")
	void shouldAddNewUser2() throws IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();
		URI uri = URI.create("http://localhost:8080/users");
		User userNew = createUser();
		System.out.println("userNew: " + userNew.toString());
		User userAdded = updateUser();
		String json = gson.toJson(userNew);
		System.out.println("json: " + json);
		final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
		System.out.println("body: " + body.toString());
		HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(body).build();
		System.out.println("request: " + request);
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println("response: " + response);
		Type userType = new TypeToken<User>() {	}.getType();
		User userActual = gson.fromJson(response.body(), userType);

		assertAll(() -> assertEquals(200, response.statusCode()), () -> assertNotNull(userActual, "Пользователь не добавляется"), () -> assertEquals(userAdded, userActual, "Пользователи не совпадают"));
	}*/

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