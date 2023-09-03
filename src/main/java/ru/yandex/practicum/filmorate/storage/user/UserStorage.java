package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

	User addNewUser(User user);

	User updateUser(User user);

	Collection<User> getAllUsers();
}
