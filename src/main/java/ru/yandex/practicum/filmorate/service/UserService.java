package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final ValidateService validateService;
    private final EventStorage eventStorage;
    private final UserDbStorage userDbStorage;

    public User create(User user) {
        validateService.checkingUserForValid(user);
        setLoginAsNameIfNameIsEmpty(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validateService.checkingUserForValid(user);
        setLoginAsNameIfNameIsEmpty(user);
        userStorage.update(user);
        return user;
    }

    public User getById(Long userId) {
        return userStorage.getById(userId);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(Long idUser, Long idFriend) {
        validateService.checkMatchingIdUsers(idUser, idFriend);
        userStorage.addFriend(idUser, idFriend);
        eventStorage.addEvent(idUser, "FRIEND", "ADD", idFriend);
    }

    /**
     * ALG_6
     */
    public void delete(Long id) {
        userStorage.delete(id);
    }

    public void deleteFriend(Long idUser, Long idFriend) {
        userStorage.deleteFriend(idUser, idFriend);
        eventStorage.addEvent(idUser, "FRIEND", "REMOVE", idFriend);
    }

    public Collection<User> getAllFriends(Long idUser) {
        return userStorage.getAllFriends(idUser);
    }


    public Collection<User> getMutualFriends(Long idUser, Long idOtherUser) {
        validateService.checkMatchingIdUsers(idUser, idOtherUser);
        return userStorage.getMutualFriends(idUser, idOtherUser);
    }

    private void setLoginAsNameIfNameIsEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

}
