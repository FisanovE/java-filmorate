package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
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
	public User addNewUser(@Valid @RequestBody User user) {
		log.info("Received request to endpoint: POST /users");
		setLoginAsNameIfNameIsEmpty(user);
		user.setId(counter);
		counter++;
		users.put(user.getId(), user);
		log.info("User added: {}.", user);
		return users.get(user.getId());
	}

	@PutMapping
	public User updateUser(@Valid @RequestBody User user) {
		log.info("Received request to endpoint: PUT /users");
		setLoginAsNameIfNameIsEmpty(user);
		for (User currentUser : users.values()) {
			if (currentUser.getEmail().equals(user.getEmail())) {
				throw new ValidationException("This email is already registered: \"" + user.getEmail() + "\"");
			}
		}
		users.put(user.getId(), user);
		log.info("User updated:  {}.", user);
		return users.get(user.getId());
	}

	@GetMapping
	public Collection<User> getAllUsers() {
		log.info("Received request to endpoint: GET /films");
		return new ArrayList<>(users.values());
	}

	private void setLoginAsNameIfNameIsEmpty(User user) {
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
	}
}
