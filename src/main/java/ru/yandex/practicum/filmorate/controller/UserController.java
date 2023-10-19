package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.ValidateService;

import java.util.*;

@Slf4j
@RestController
@Component
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final ValidateService validateService;

    @PostMapping
    public User addNewUser(@RequestBody User user) {
        validateService.checkingUserForValid(user);
        log.info("Create user");
        return userService.addNewUser(user);
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        validateService.checkIdNotNull(user.getId());
        log.info("Update user {}", user.getId());
        return userService.updateUser(user);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable(required = false) String id) {
        log.info("Get user {}", id);
        return userService.getUserById(Long.parseLong(id));
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Get users");
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Update user{}, add friend {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable(required = false) Long id, @PathVariable(required = false) Long friendId) {
        log.info("Update user{}, delete friend {}", id, friendId);
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getAllFriendsOfUser(@PathVariable(required = false) Long id) {
        log.info("Get friends of user {}", id);
        return userService.getAllFriendsOfUser(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getMutualFriends(@PathVariable(required = false) Long id, @PathVariable(required = false) Long otherId) {
        log.info("Get mutual friends of users {} & {}", id, otherId);
        return userService.getMutualFriends(id, otherId);
    }

    /**
     * ALG_6
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.info("Delete user {}", id);
        userService.deleteUser(id);
    }

    /**
     * ALG_4
     */
    @GetMapping("/{id}/recommendations")
    public List<Film> getFilmsRecommendationsForUser(@PathVariable Long id) {
        log.info("Get films recommendations for user {}", id);
        return userService.getFilmsRecommendationsForUser(id);
    }

    /**
     * ALG_5
     */
    @GetMapping("/{id}/feed")
    public Collection<Event> getEvent(@PathVariable("id") Long userId) {
        log.info("Get events of user {}", userId);
        return userService.getEvent(userId);
    }

}
