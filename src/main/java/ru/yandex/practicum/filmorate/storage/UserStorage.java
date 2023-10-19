package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;


public interface UserStorage {

    User create(User user);

    void update(User user);

    Collection<User> getAll();

    User getById(Long id);

    void addFriend(Long idUser, Long idFriend);

    void deleteFriend(Long idUser, Long idFriend);

    Collection<User> getAllFriends(Long idUser);

    Collection<User> getMutualFriends(Long idUser, Long idOtherUser);

    /**
     * ALG_6
     */
    void delete(Long id);

    /**
     * ALG_4
     */
    List<Film> getFilmsRecommendationsForUser(Long id);

    /**
     * ALG5
     */
    List<Event> getEvents(Long userId);
}
