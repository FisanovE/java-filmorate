package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final ValidateService validateService;

    public User addNewUser(User user) {
        setLoginAsNameIfNameIsEmpty(user);
        return userStorage.addNewUser(user);
    }

    public User updateUser(User user) {
        validateService.checkContainsUserInDatabase(user.getId());
        validateService.checkingUserForValid(user);
        setLoginAsNameIfNameIsEmpty(user);
        return userStorage.updateUser(user);
    }

    public User getUserById(Long userId) {
        validateService.checkContainsUserInDatabase(userId);
        return userStorage.getUserById(userId);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
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
    public void deleteUser(Long id) {
        validateService.checkContainsUserInDatabase(id);
        userStorage.deleteUser(id);
    }

    public void deleteFriend(Long idUser, Long idFriend) {
        validateService.checkContainsUserInDatabase(idUser);
        userStorage.deleteFriend(idUser, idFriend);
    }

    public Collection<User> getAllFriendsOfUser(Long idUser) {
        validateService.checkContainsUserInDatabase(idUser);
        return userStorage.getAllFriendsOfUser(idUser);
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
     * ALG_4
     */
    public List<Film> getFilmsRecommendationsForUser(Long id) {
        validateService.checkContainsUserInDatabase(id);
        return userStorage.getFilmsRecommendationsForUser(id);
    }

    /**
     * ALG_5
     */
    public Collection<Event> getEvent(Long userId) {
        validateService.checkContainsUserInDatabase(userId);
        return userStorage.getEvents(userId);
    }
}
