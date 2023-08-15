package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
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
		log.info("Received request to endpoint: POST /users");
		checkingUserForValid(user);
		setLoginAsNameIfNameIsEmpty(user);

		user.setId(counter);
		counter++;
		users.put(user.getId(), user);
		log.info("User added: {}.", user);
		return users.get(user.getId());
	}

	@PutMapping
	public User updateUser(@Valid @RequestBody User user) throws ValidationException {
		log.info("Received request to endpoint: PUT /users");
		checkingUserForValid(user);
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

	private void checkingUserForValid(User user) throws ValidationException {
		if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
			throw new ValidationException("Invalid e-mail format: \"" + user.getEmail() + "\"");
		}
		if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
			throw new ValidationException("Wrong login: \"" + user.getLogin() + "\"");
		}
		if (user.getBirthday().isAfter(LocalDate.now())) {
			throw new ValidationException("Date of birth cannot be in the future: \"" + user.getBirthday() + "\"");
		}
	}

	private void setLoginAsNameIfNameIsEmpty(User user) {
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
	}

}
