package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public User addNewUser(User user) {
        setLoginAsNameIfNameIsEmpty(user);
        return userStorage.addNewUser(user);
    }

    public User updateUser(User user) {
        setLoginAsNameIfNameIsEmpty(user);
        return userStorage.updateUser(user);
    }

    public User getUserById(Long userId) {
        return userStorage.getUserById(userId);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(Long idUser, Long idFriend) {
        if (idUser.equals(idFriend)) {
            throw new IllegalArgumentException("You can't add yourself as a friend:  " + idFriend);
        } else {
            userStorage.addFriend(idUser, idFriend);
        }
    }

    public void deleteUser(Long id) {
        userStorage.deleteUser(id);
    }

    public void deleteFriend(Long idUser, Long idFriend) {
        userStorage.deleteFriend(idUser, idFriend);
    }

    public Collection<User> getAllFriendsOfUser(Long idUser) {
        return userStorage.getAllFriendsOfUser(idUser);
    }


    public Collection<User> getMutualFriends(Long idUser, Long idOtherUser) {
        return userStorage.getMutualFriends(idUser, idOtherUser);
    }

    private void setLoginAsNameIfNameIsEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
