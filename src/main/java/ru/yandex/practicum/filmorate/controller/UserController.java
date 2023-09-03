package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@Component
@RequiredArgsConstructor
@RequestMapping ("/users")
public class UserController {
	private final UserService userService;

	@PostMapping
	public User addNewUser(@Valid @RequestBody User user) {
		log.info("Received request to endpoint: POST /users");
		return userService.addNewUser(user);
	}

	@PutMapping
	public User updateUser(@Valid @RequestBody User user) {
		log.info("Received request to endpoint: PUT /users");
		return userService.updateUser(user);
	}

	@GetMapping ("/{id}")
	public User getUserById(@PathVariable (required = false) String id) {
		log.info("Received request to endpoint: GET /users/{}", id);
		return userService.getUserById(Long.parseLong(id));
	}

	@GetMapping
	public Collection<User> getAllUsers() {
		log.info("Received request to endpoint: GET /users");
		return userService.getAllUsers();
	}

	@PutMapping ("/{id}/friends/{friendId}")
	public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
		log.info("Received request to endpoint: PUT /users/{}/friends/{}", id, friendId);
		Long id1 = id;
		if (id1.equals(friendId)) {
			throw new IllegalArgumentException("You can't add yourself as a friend:  " + friendId);
		} else {
			userService.addFriend(id, friendId);
		}
	}

	@DeleteMapping ("/{id}/friends/{friendId}")
	public void deleteFriend(@PathVariable (required = false) Long id, @PathVariable (required = false) Long friendId) {
		log.info("Received request to endpoint: DELETE /users/{}/friends/{}", id, friendId);
		userService.deleteFriend(id, friendId);
	}

	@GetMapping ("/{id}/friends")
	public Collection<User> getAllFriendsOfUser(@PathVariable (required = false) Long id) {
		log.info("Received request to endpoint: GET /users/{}/friends", id);
		return userService.getAllFriendsOfUser(id);
	}

	@GetMapping ("/{id}/friends/common/{otherId}")
	public Collection<User> getMutualFriends(@PathVariable (required = false) Long id, @PathVariable (required = false) Long otherId) {
		log.info("Received request to endpoint: GET /users/{}/friends/common/{}", id, otherId);
		return userService.getMutualFriends(id, otherId);
	}
}
