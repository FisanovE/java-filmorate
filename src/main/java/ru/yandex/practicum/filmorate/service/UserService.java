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
		if (idUser.equals(idFriend)) {
			throw new IllegalArgumentException("You can't add yourself as a friend:  " + idFriend);
		} else {
			User user = userStorage.getUserById(idUser);
			User friend = userStorage.getUserById(idFriend);
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
			userStorage.updateUser(user);
			userStorage.updateUser(friend);
		}
	}

	public void deleteFriend(Long idUser, Long idFriend) {
		User user = userStorage.getUserById(idUser);
		User friend = userStorage.getUserById(idFriend);
		HashMap<Long, Boolean> userFriends;
		HashMap<Long, Boolean> friendFriends;
		if (user.getFriendsId() == null || !user.getFriendsId().containsKey(idFriend)) {
			throw new NotFoundException("User ID is missing from friends:  " + idFriend);
		} else {
			userFriends = (HashMap<Long, Boolean>) user.getFriendsId();
			user.setFriendsId(userFriends);
		}
		userFriends.remove(idFriend);
		if (friend.getFriendsId() == null || !friend.getFriendsId().containsKey(idUser)) {
			throw new NotFoundException("User ID is missing from friends:  " + idUser);
		} else {
			friendFriends = (HashMap<Long, Boolean>) friend.getFriendsId();
		}
		friendFriends.remove(idUser);
		friend.setFriendsId(friendFriends);
		user.setFriendsId(userFriends);
		userStorage.updateUser(user);
		userStorage.updateUser(friend);
	}

	public Collection<User> getAllFriendsOfUser(Long idUser) {
		ArrayList<User> friends = new ArrayList<>();
		if ((userStorage.getUserById(idUser).getFriendsId() == null)) {
			return new ArrayList<>();
		}
		HashMap<Long, Boolean> idAllFriends = (HashMap<Long, Boolean>) userStorage.getUserById(idUser).getFriendsId();
		for (Long id : idAllFriends.keySet()) {
			friends.add(userStorage.getUserById(id));
		}
		/*for (Long id : idAllFriends.keySet()) {
			if(idAllFriends.get(id) == true){
				friends.add(userStorage.getUserById(id));
			}
		}*/
		friends.sort((o1, o2) -> (int) (o1.getId() - o2.getId()));
		return friends;
	}


	public Collection<User> getMutualFriends(Long idUser, Long idOtherUser) {
		if (userStorage.getUserById(idUser).getFriendsId() == null || userStorage.getUserById(idOtherUser)
																				 .getFriendsId() == null) {
			return new HashSet<>();
		}
		Set<Long> mutualId;
		Set<User> mutualFriends = new HashSet<>();
		Collection<Long> userFriends = userStorage.getUserById(idUser).getFriendsId().keySet();
		Collection<Long> otherUserFriends = userStorage.getUserById(idOtherUser).getFriendsId().keySet();
		mutualId = userFriends.stream().filter(otherUserFriends::contains).collect(Collectors.toSet());
		for (Long id : mutualId) {
			mutualFriends.add(userStorage.getUserById(id));
		}
		return mutualFriends;
	}
}
