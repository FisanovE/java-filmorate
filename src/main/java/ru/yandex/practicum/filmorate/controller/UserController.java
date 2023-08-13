package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping ("/users")
public class UserController {
	private final Map<Integer, User> users = new HashMap<>();
	private int counter = 1;

	@PostMapping
	public User addNewUser(@Valid @RequestBody User user) throws ValidationException {
		log.info("Получен запрос к эндпоинту: POST /users");
		if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
			throw new ValidationException("Не верный формат электронной почты: \"" + user.getEmail() + "\"");
		}
		if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
			throw new ValidationException("Не верный логин: \"" + user.getLogin() + "\"");
		}
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
		if (user.getBirthday().isAfter(LocalDate.now())) {
			throw new ValidationException("Дата рождения не может быть в будущем: \"" + user.getBirthday() + "\"");
		}
		user.setId(counter);
		counter++;
		users.put(user.getId(), user);
		log.info("Добавлен пользователь {}.", user);
		return users.get(user.getId());
	}

	@PutMapping
	public User updateUser(@Valid @RequestBody User user) throws ValidationException {
		log.info("Получен запрос к эндпоинту: PUT /users");
		if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
			throw new ValidationException("Не верный формат электронной почты: \"" + user.getEmail() + "\"");
		}
		if (user.getLogin().isBlank()) {
			throw new ValidationException("Не верный логин: \"" + user.getLogin() + "\"");
		}
		if (user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
		if (user.getBirthday().isAfter(LocalDate.now())) {
			throw new ValidationException("Дата рождения не может быть в будущем: \"" + user.getBirthday() + "\"");
		}
		for (User currentUser : users.values()) {
			if (currentUser.getEmail().equals(user.getEmail())) {
				throw new ValidationException("Этот email уже зарегистрирован: \"" + user.getEmail() + "\"");
			}
		}

		users.put(user.getId(), user);
		log.info("Изменён пользователь {}.", user);
		return users.get(user.getId());
	}

	@GetMapping
	public Collection<User> getAllUsers() {
		return new ArrayList<>(users.values());
	}

}
