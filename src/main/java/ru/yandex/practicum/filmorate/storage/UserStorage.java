package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

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

    SqlRowSet getUserRow(Long id);

}
