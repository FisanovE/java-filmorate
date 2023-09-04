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
		log.info("Endpoint -> Create user");
		return userService.addNewUser(user);
	}

	@PutMapping
	public User updateUser(@Valid @RequestBody User user) {
		log.info("Endpoint -> Update user");
		return userService.updateUser(user);
	}

	@GetMapping ("/{id}")
	public User getUserById(@PathVariable (required = false) String id) {
		log.info("Endpoint -> Get user {}", id);
		return userService.getUserById(Long.parseLong(id));
	}

	@GetMapping
	public Collection<User> getAllUsers() {
		log.info("Endpoint -> Get users");
		return userService.getAllUsers();
	}

	@PutMapping ("/{id}/friends/{friendId}")
	public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
		log.info("Endpoint -> Update user{}, add friend {}", id, friendId);
		userService.addFriend(id, friendId);
	}

	@DeleteMapping ("/{id}/friends/{friendId}")
	public void deleteFriend(@PathVariable (required = false) Long id, @PathVariable (required = false) Long friendId) {
		log.info("Endpoint -> Update user{}, delete friend {}", id, friendId);
		userService.deleteFriend(id, friendId);
	}

	@GetMapping ("/{id}/friends")
	public Collection<User> getAllFriendsOfUser(@PathVariable (required = false) Long id) {
		log.info("Endpoint -> Get friends of user {}", id);
		return userService.getAllFriendsOfUser(id);
	}

	@GetMapping ("/{id}/friends/common/{otherId}")
	public Collection<User> getMutualFriends(@PathVariable (required = false) Long id, @PathVariable (required = false) Long otherId) {
		log.info("Endpoint -> Get mutual friends of users {} & {}", id, otherId);
		return userService.getMutualFriends(id, otherId);
	}
}
