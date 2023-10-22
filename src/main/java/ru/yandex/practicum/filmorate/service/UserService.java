package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final ValidateService validateService;

    public User create(User user) {
        setLoginAsNameIfNameIsEmpty(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validateService.checkContainsUserInDatabase(user.getId());
        validateService.checkingUserForValid(user);
        setLoginAsNameIfNameIsEmpty(user);
        userStorage.update(user);
        return user;
    }

    public User getById(Long userId) {
        validateService.checkContainsUserInDatabase(userId);
        return userStorage.getById(userId);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(Long idUser, Long idFriend) {
        validateService.checkMatchingIdUsers(idUser, idFriend);
        validateService.checkContainsUserInDatabase(idUser);
        validateService.checkContainsUserInDatabase(idFriend);
        userStorage.addFriend(idUser, idFriend);
    }

    /**
     * ALG_6
     */
    public void delete(Long id) {
        validateService.checkContainsUserInDatabase(id);
        userStorage.delete(id);
    }

    public void deleteFriend(Long idUser, Long idFriend) {
        validateService.checkContainsUserInDatabase(idUser);
        userStorage.deleteFriend(idUser, idFriend);
    }

    public Collection<User> getAllFriends(Long idUser) {
        validateService.checkContainsUserInDatabase(idUser);
        return userStorage.getAllFriends(idUser);
    }


    public Collection<User> getMutualFriends(Long idUser, Long idOtherUser) {
        validateService.checkMatchingIdUsers(idUser, idOtherUser);
        validateService.checkContainsUserInDatabase(idUser);
        validateService.checkContainsUserInDatabase(idOtherUser);
        return userStorage.getMutualFriends(idUser, idOtherUser);
    }

    private void setLoginAsNameIfNameIsEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    /**
     * ALG_5
     */
    public Collection<Event> getEvents(Long userId) {
        validateService.checkContainsUserInDatabase(userId);
        return userStorage.getEvents(userId);
    }
}
