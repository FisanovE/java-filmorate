package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;


public interface UserStorage {

    User addNewUser(User user);

    User updateUser(User user);

    Collection<User> getAllUsers();

    User getUserById(Long id);

    void addFriend(Long idUser, Long idFriend);

    void deleteFriend(Long idUser, Long idFriend);

    Collection<User> getAllFriendsOfUser(Long idUser);

    Collection<User> getMutualFriends(Long idUser, Long idOtherUser);

    /** ALG_6 */
    void deleteUser(Long id);
}
