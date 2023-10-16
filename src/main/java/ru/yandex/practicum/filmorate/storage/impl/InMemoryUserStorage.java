package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emails = new HashMap<>();
    private Long counter = 1L;

    @Override
    public User addNewUser(User user) {
        checkingRepeat(emails, user);
        user.setId(counter);
        counter++;
        users.put(user.getId(), user);
        emails.put(user.getEmail(), user.getId());
        log.info("User added: {}.", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Invalid User ID:  " + user.getId());
        }
        users.put(user.getId(), user);
        log.info("User updated:  {}.", user);
        return user;
    }

    @Override
    public User getUserById(Long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        throw new NotFoundException("Invalid User ID:  " + id);
    }

    @Override
    public Collection<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }


    private void checkingRepeat(Map<String, Long> emails, User user) {
        if (emails.containsKey(user.getEmail())) {
            throw new ValidationException("This email is already registered: \"" + user.getEmail() + "\"");
        }
    }

    @Override
    public void addFriend(Long idUser, Long idFriend) {
        User user = getUserById(idUser);
        User friend = getUserById(idFriend);
        HashMap<Long, Boolean> userFriends;
        HashMap<Long, Boolean> friendFriends;
        if (user.getFriendsId() == null) {
            userFriends = new HashMap<>();
        } else {
            userFriends = (HashMap<Long, Boolean>) user.getFriendsId();
        }
        userFriends.put(idFriend, false);
        user.setFriendsId(userFriends);
        if (friend.getFriendsId() == null) {
            friendFriends = new HashMap<>();
        } else {
            friendFriends = (HashMap<Long, Boolean>) friend.getFriendsId();
        }
        friendFriends.put(idUser, false);
        friend.setFriendsId(friendFriends);
        updateUser(user);
        updateUser(friend);
    }

    @Override
    public void deleteFriend(Long idUser, Long idFriend) {
        User user = getUserById(idUser);
        User friend = getUserById(idFriend);
        HashMap<Long, Boolean> userFriends;
        HashMap<Long, Boolean> friendFriends;
        if (user.getFriendsId() == null || !user.getFriendsId().containsKey(idFriend)) {
            throw new NotFoundException("User ID is missing in friends:  " + idFriend);
        } else {
            userFriends = (HashMap<Long, Boolean>) user.getFriendsId();
            user.setFriendsId(userFriends);
        }
        userFriends.remove(idFriend);
        if (friend.getFriendsId() == null || !friend.getFriendsId().containsKey(idUser)) {
            throw new NotFoundException("User ID is missing in friends:  " + idUser);
        } else {
            friendFriends = (HashMap<Long, Boolean>) friend.getFriendsId();
        }
        friendFriends.remove(idUser);
        friend.setFriendsId(friendFriends);
        user.setFriendsId(userFriends);
        updateUser(user);
        updateUser(friend);
    }

    @Override
    public Collection<User> getAllFriendsOfUser(Long idUser) {
        ArrayList<User> friends = new ArrayList<>();
        if ((getUserById(idUser).getFriendsId() == null)) {
            return new ArrayList<>();
        }
        HashMap<Long, Boolean> idAllFriends = (HashMap<Long, Boolean>) getUserById(idUser).getFriendsId();
        for (Long id : idAllFriends.keySet()) {
            friends.add(getUserById(id));
        }

        friends.sort((o1, o2) -> (int) (o1.getId() - o2.getId()));
        return friends;
    }

    @Override
    public Collection<User> getMutualFriends(Long idUser, Long idOtherUser) {
        if (getUserById(idUser).getFriendsId() == null || getUserById(idOtherUser).getFriendsId() == null) {
            return new HashSet<>();
        }
        Set<Long> mutualId;
        Set<User> mutualFriends = new HashSet<>();
        Collection<Long> userFriends = getUserById(idUser).getFriendsId().keySet();
        Collection<Long> otherUserFriends = getUserById(idOtherUser).getFriendsId().keySet();
        mutualId = userFriends.stream().filter(otherUserFriends::contains).collect(Collectors.toSet());
        for (Long id : mutualId) {
            mutualFriends.add(getUserById(id));
        }
        return mutualFriends;
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }

    /**
     * ALG_4
     */
    @Override
    public List<Film> getFilmsRecommendationsForUser(Long id) {
        /*NOT IMPLEMENTED*/
        return Collections.emptyList();
    }

    /**
     * ALG5
     */
    @Override
    public List<Event> getEvents(Long userId) {
        return Collections.emptyList();
    }
}
