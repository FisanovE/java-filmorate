package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.DateUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest
class FilmorateApplicationTests {

	Gson gson = new Gson();

	@BeforeEach
	void init() {
		String[] args = new String[0];
		SpringApplication.run(FilmorateApplication.class, args);
	}

	@Test
	@DisplayName ("Добавление нового пользователя ")
	void shouldAddNewUser() throws IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();
		URI uri = URI.create("http://localhost:8080/users");
		User userNew = new User(0, "dolore","Nick Name", "mail@mail.ru", LocalDate.parse("1946-08-20T09:40:00" +
				".000000000", DateUtils.formatter));
		User userAdded = new User(1,"dolore","Nick Name", "mail@mail.ru", LocalDate.parse("1946-08-20T09:40:00" +
				".000000000", DateUtils.formatter));
		String json = gson.toJson(userNew);

		final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
		HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(body).build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		int id = Integer.parseInt(response.body());

		Type userType = new TypeToken<User>() {	}.getType();
		User userActual = gson.fromJson(response.body(), userType);

		assertAll(() -> assertEquals(200, response.statusCode()),
				() -> assertNotNull(userActual, "Задача не добавляется"),
				() -> assertEquals(userAdded, userActual, "Задачи не совпадают"));
	}

}
