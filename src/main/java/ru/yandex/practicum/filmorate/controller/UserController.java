package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.*;

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
		checkingRepeat(users, user);
		user.setId(counter);
		counter++;
		users.put(user.getId(), user);
		log.info("User added: {}.", user);
		return user;
	}

	@PutMapping
	public User updateUser(@Valid @RequestBody User user) {
		log.info("Received request to endpoint: PUT /users");
		setLoginAsNameIfNameIsEmpty(user);
		checkingRepeat(users, user);
		users.put(user.getId(), user);
		log.info("User updated:  {}.", user);
		return user;
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

	private void checkingRepeat(Map<Integer, User> users, User user) {
		for (User currentUser : users.values()) {
			if (Objects.equals(currentUser.getEmail(), user.getEmail()) && !Objects.equals(currentUser.getId(),
					user.getId())) {
				throw new ValidationException("This email is already registered: \"" + user.getEmail() + "\"");
			}
		}
	}
}
