package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

	private final Map<Long, User> users = new HashMap<>();
	private final Map<String, Long> emails = new HashMap<>();
	private Long counter = 1L;

	@Override
	public User addNewUser(User user) {
		setLoginAsNameIfNameIsEmpty(user);
		checkingRepeat(emails, user);
		user.setId(counter);
		counter++;
		users.put(user.getId(), user);
		emails.put(user.getEmail(), user.getId());
		log.info("User added: {}.", user);
		return user;
	}

	@Override
	public User updateUser(User user) {
		if (!users.containsKey(user.getId())) {
			throw new NotFoundException("Invalid User ID:  " + user.getId());
		}
		setLoginAsNameIfNameIsEmpty(user);
		users.put(user.getId(), user);
		log.info("User updated:  {}.", user);
		return user;
	}

	public User getUserById(Long id) {
		if (users.containsKey(id)) {
			return users.get(id);
		}
		throw new NotFoundException("Invalid User ID:  " + id);
	}

	@Override
	public Collection<User> getAllUsers() {
		return new ArrayList<>(users.values());
	}

	private void setLoginAsNameIfNameIsEmpty(User user) {
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
	}

	private void checkingRepeat(Map<String, Long> emails, User user) {
		if (emails.containsKey(user.getEmail())) {
			throw new ValidationException("This email is already registered: \"" + user.getEmail() + "\"");
		}
	}
}
