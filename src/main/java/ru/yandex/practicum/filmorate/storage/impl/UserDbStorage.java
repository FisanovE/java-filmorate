package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
//@Qualifier("userDbStorage")
//@Primary
public class UserDbStorage implements UserStorage {
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public UserDbStorage(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public User addNewUser(User user) {
		String sqlRequest = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, user.getEmail());
			ps.setString(2, user.getLogin());
			ps.setString(3, user.getName());
			ps.setDate(4, Date.valueOf(user.getBirthday()));
			return ps;
		}, keyHolder);

		Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
		user.setId(generatedId);

		log.info("User added: {} {}", user.getId(), user.getLogin());
		return user;

		/*String sqlRequest = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
		//jdbcTemplate.update(sqlRequest, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
		final String SQL = "INSERT INTO ... RETUNING id";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(SQL,
					Statement.RETURN_GENERATED_KEYS);

			return ps;
		}, keyHolder);

		return keyHolder.getKey().intValue();*/
	}

	/*@Override
	public User addNewUser(User user) {
		try {
			// Проверяем, что объект user и его свойства не являются null
			if (user == null || user.getEmail() == null || user.getLogin() == null || user.getName() == null || user.getBirthday() == null) {
				// Обработка ситуации, когда одно из полей равно null
				log.error("User or user properties are null.");
				return null; // Или выбросить исключение, в зависимости от требований
			}
			String sqlRequest = "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES (?, ?, ?, ?)";
			// Выполняем вставку в базу данных в рамках транзакции
			jdbcTemplate.update(sqlRequest, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());

			log.info("User added: {}", user.getLogin());
			return user;
		} catch (DataAccessException e) {
			// Обработка исключения при ошибке выполнения SQL-запроса
			log.error("Error while adding a new user: {}", e.getMessage(), e);
			// Можно выбросить собственное исключение или вернуть null
			throw new RuntimeException("Error while adding a new user", e); // Пример выбрасывания исключения
		}
	}*/

	/*@Override
	public User updateUser(User user) {
		String sqlRequest = "UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE USER_ID = ?";
		jdbcTemplate.update(sqlRequest, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
		log.info("User update: {} {}", user.getId(), user.getLogin());
		return user;
	}*/

	@Override
	public User updateUser(User user) {
		String sqlRequest = "UPDATE USERS SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE USER_ID = ?";
		int rowsUpdated = jdbcTemplate.update(sqlRequest, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

		if (rowsUpdated == 0) {
			throw new NotFoundException("Invalid User ID:  " + user.getId());
		}

		log.info("User update: {} {}", user.getId(), user.getLogin());
		return user;
	}

	@Override
	public Collection<User> getAllUsers() {
		//return jdbcTemplate.queryForList("SELECT * FROM USERS ORDER BY USER_ID", User.class);

		String sql = "SELECT * FROM USERS ORDER BY USER_ID";
		return jdbcTemplate.query(sql, new UserRowMapper());
	}

	public User getUserById(Long id) {
		SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users where user_id = ?", id);
		if (userRows.next()) {
			log.info("User found: {} {}", userRows.getString("user_id"), userRows.getString("name"));

			return User.builder().id(userRows.getLong("user_id")).email(userRows.getString("email"))
					   .login(userRows.getString("login")).name(userRows.getString("name"))
					   .birthday(Objects.requireNonNull(userRows.getDate("birthday")).toLocalDate()).build();
		} else {
			log.info("Invalid User ID: {}", id);
			throw new NotFoundException("Invalid User ID:  " + id);
		}
	}

	@Override
	public void addFriend(Long idUser, Long idFriend) {
		//SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from friends where user_id= ? AND friend_id= ?",
		// idUser, idFriend);
		if (idFriend <= 0) {
			throw new NotFoundException("Invalid User ID:  " + idFriend);
		}
		String sqlRequest = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
		int rowsUpdated = jdbcTemplate.update(sqlRequest, idUser, idFriend);

		if (rowsUpdated == 0) {
			throw new NotFoundException("User ID is missing in friends:  " + idFriend);
		}
		log.info("Added friends ID: {}", idFriend);
		/*if (userRows.next()) {
			String sqlRequest = "INSERT INTO friends (user_id, friend_id, friend_confirm) VALUES (?, ?, ?)";
			jdbcTemplate.queryForRowSet(sqlRequest, idUser, idFriend, false);
		} else if (!userRows.getBoolean("friend_confirm")) {
			String sqlRequest = "UPDATE USERS SET friend_confirm = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE USER_ID = ?";
			int rowsUpdated = jdbcTemplate.update(sqlRequest, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
		}*/
	}


	@Override
	public void deleteFriend(Long idUser, Long idFriend) {
		String sql = "DELETE FROM friends WHERE USER_ID = ? AND FRIEND_ID = ?";
		jdbcTemplate.update(sql, idUser, idFriend);
	}

	@Override
	public Collection<User> getAllFriendsOfUser(Long idUser) {
		String sql = "SELECT * FROM USERS WHERE user_ID IN (select friend_id FROM friends WHERE user_id = ?) ORDER BY USER_ID";

		return jdbcTemplate.query(sql, new UserRowMapper(), idUser);
	}

	/*public Collection<User> getAllFriendsOfUser(Long idUser) {
		String sql = "SELECT * FROM USERS WHERE user_ID IN (select friend_id FROM friends WHERE user_id = " + idUser +
				" ORDER BY friend_id) ORDER BY USER_ID";
		return jdbcTemplate.query(sql, (rs, rowNum) -> User.builder()
														   .id(rs.getLong("user_id"))
														   .email(rs.getString("email"))
														   .login(rs.getString("login"))
														   .name(rs.getString("name"))
														   .birthday(Objects.requireNonNull(rs.getDate("birthday"))
																			.toLocalDate()).build());
	}*/

	@Override
	public Collection<User> getMutualFriends(Long idUser, Long idOtherUser) {
		String sql = "SELECT * FROM USERS WHERE user_ID IN (SELECT friend_id FROM (SELECT friend_id FROM friends WHERE " +
						"user_id IN (?, ?) AND friend_id NOT IN (?, ?) ORDER BY friend_id) AS ids GROUP BY " +
						"friend_id HAVING count(friend_id)>1) ORDER BY USER_ID";

		return jdbcTemplate.query(sql, new UserRowMapper(), idUser, idOtherUser, idUser, idOtherUser);
	}

}
