package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final InMemoryUserStorage userStorage;

	public User addNewUser(User user) {
		return userStorage.addNewUser(user);
	}

	public User updateUser(User user) {
		return userStorage.updateUser(user);
	}

	public User getUserById(Long userId) {
		return userStorage.getUserById(userId);
	}

	public Collection<User> getAllUsers() {
		return userStorage.getAllUsers();
	}

	public void addFriend(Long idUser, Long idFriend) {

		User user = userStorage.getUserById(idUser);
		User friend = userStorage.getUserById(idFriend);
		HashSet<Long> userFriends;
		HashSet<Long> friendFriends;
		if (user.getFriends() == null) {
			userFriends = new HashSet<>();
		} else {
			userFriends = user.getFriends();
		}
		userFriends.add(idFriend);
		user.setFriends(userFriends);
		if (friend.getFriends() == null) {
			friendFriends = new HashSet<>();
		} else {
			friendFriends = friend.getFriends();
		}
		friendFriends.add(idUser);
		friend.setFriends(friendFriends);
		userStorage.updateUser(user);
		userStorage.updateUser(friend);
	}

	public void deleteFriend(Long idUser, Long idFriend) {
		User user = userStorage.getUserById(idUser);
		User friend = userStorage.getUserById(idFriend);
		HashSet<Long> userFriends;
		HashSet<Long> friendFriends;
		if (user.getFriends() == null || !user.getFriends().contains(idFriend)) {
			throw new NotFoundException("User ID is missing from friends:  " + idFriend);
		} else {
			userFriends = user.getFriends();
			user.setFriends(userFriends);
		}
		userFriends.remove(idFriend);
		if (friend.getFriends() == null || !friend.getFriends().contains(idUser)) {
			throw new NotFoundException("User ID is missing from friends:  " + idUser);
		} else {
			friendFriends = friend.getFriends();
		}
		friendFriends.remove(idUser);
		friend.setFriends(friendFriends);
		user.setFriends(userFriends);
		userStorage.updateUser(user);
		userStorage.updateUser(friend);
	}

	public Collection<User> getAllFriendsOfUser(Long idUser) {
		ArrayList<User> friends = new ArrayList<>();
		if ((userStorage.getUserById(idUser).getFriends() == null)) {
			return new ArrayList<>();
		}
		HashSet<Long> idAllFriends = userStorage.getUserById(idUser).getFriends();
		for (Long id : idAllFriends) {
			friends.add(userStorage.getUserById(id));
		}
		Collections.sort(friends, (o1, o2) -> (int) (o1.getId() - o2.getId()));
		return friends;
	}


	public Collection<User> getMutualFriends(Long idUser, Long idOtherUser) {
		if (userStorage.getUserById(idUser).getFriends() == null || userStorage.getUserById(idOtherUser)
																			   .getFriends() == null) {
			return new HashSet<>();
		}
		Set<Long> mutualId;
		Set<User> mutualFriends = new HashSet<>();
		Collection<Long> userFriends = userStorage.getUserById(idUser).getFriends();
		Collection<Long> otherUserFriends = userStorage.getUserById(idOtherUser).getFriends();
		mutualId = userFriends.stream().filter(otherUserFriends::contains).collect(Collectors.toSet());
		for (Long id : mutualId) {
			mutualFriends.add(userStorage.getUserById(id));
		}
		return mutualFriends;
	}
}
