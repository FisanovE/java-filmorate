package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@Component
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public User addNewUser(@RequestBody User user) {
        log.info("Endpoint -> Create user");
        checkingUserForValid(user);
        return userService.addNewUser(user);
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Endpoint -> Update user");
        checkingUserForValid(user);
        return userService.updateUser(user);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable(required = false) String id) {
        log.info("Endpoint -> Get user {}", id);
        return userService.getUserById(Long.parseLong(id));
    }

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Endpoint -> Get users");
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Endpoint -> Update user{}, add friend {}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable(required = false) Long id, @PathVariable(required = false) Long friendId) {
        log.info("Endpoint -> Update user{}, delete friend {}", id, friendId);
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getAllFriendsOfUser(@PathVariable(required = false) Long id) {
        log.info("Endpoint -> Get friends of user {}", id);
        return userService.getAllFriendsOfUser(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getMutualFriends(@PathVariable(required = false) Long id, @PathVariable(required = false) Long otherId) {
        log.info("Endpoint -> Get mutual friends of users {} & {}", id, otherId);
        return userService.getMutualFriends(id, otherId);
    }

    private void checkingUserForValid(User user) throws ValidationException {
        String emailRegex = "^([a-z0-9_\\.-]+)@([a-z0-9_\\.-]+)\\.([a-z\\.]{2,6})$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(user.getEmail());
        if (!matcher.matches()) {
            throw new ValidationException("Invalid e-mail format: \"" + user.getEmail() + "\"");
        }
        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Login field must not be empty and contain spaces: \"" + user.getLogin() + "\"");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth cannot be in the future: \"" + user.getBirthday() + "\"");
        }
    }

    /**
     * ALG_6
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    /**
     * ALG_4
     */
    @GetMapping ("/{id}/recommendations")
    public List<Film> getFilmsRecommendationsForUser(@PathVariable Long id) {
        log.info("ALG_4. Endpoint -> Get films recommendations for user {}", id);
        return userService.getFilmsRecommendationsForUser(id);
    }
}
