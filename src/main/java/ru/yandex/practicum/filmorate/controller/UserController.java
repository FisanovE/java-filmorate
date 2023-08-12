package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//@Controller
@Slf4j
@RestController
@RequestMapping ("/users")
public class UserController {
	private final Map<Integer, User> users = new HashMap<>();
	private int counter = 1;

	@PostMapping
	public User addNewUser(@Valid @RequestBody User user) {
		log.info("Получен запрос к эндпоинту: POST /users");
		if (user.getName() == null || user.getName().isEmpty()) {
			user.setName(user.getLogin());
		}
		user.setId(counter);
		counter++;
		users.put(user.getId(), user);
		return users.get(user.getId());
		//return user;
	}

	@PutMapping
	public User updateUser(@Valid @RequestBody User user) throws ValidationException {
		log.info("Получен запрос к эндпоинту: PUT /users");
		if (user.getName() == null || user.getName().isEmpty()) {
			user.setName(user.getLogin());
		}
		for (User us : users.values()) {
			if (us.getEmail().equals(user.getEmail())) {
				throw new ValidationException("Пользователь с электронной почтой " + user.getEmail() + " уже зарегистрирован.");
			}
		}
		users.put(user.getId(), user);
		return users.get(user.getId());
		//return user;
	}

	@GetMapping
	public Collection<User> getAllUsers() {
		return users.values();
	}

}
