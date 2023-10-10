package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor (onConstructor_ = @Autowired)
class FilmorateApplicationTests {

	@Qualifier("userDbStorage")
	private final UserStorage userStorage;
	@Qualifier ("filmDbStorage")
	private final FilmStorage filmStorage;

	@Qualifier ("directorDbStorage")
	private final DirectorStorage directorStorage;
	private User user;
	private Film film;

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Добавление нового пользователя")
	void shouldAddNewUser() {
		User userNew = createUser();

		User userAdded = userStorage.addNewUser(userNew);

		List<User> list = new ArrayList<>(userStorage.getAllUsers());
		assertThat(list).isNotEmpty().hasSize(1).contains(userAdded);
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получеие пользователя по ID")
	void shouldReturnUserById() {
		User userNew = createUser();
		userStorage.addNewUser(userNew);

		Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(1L));

		assertThat(userOptional).isPresent()
								.hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 1L));
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Обновление пользователя")
	void shouldUpdateUser() {
		User userNew = createUser();
		userStorage.addNewUser(userNew);

		User userUpdate = updateUser();

		userStorage.updateUser(userUpdate);
		List<User> listUpdate = new ArrayList<>(userStorage.getAllUsers());
		assertThat(listUpdate).isNotEmpty().hasSize(1).contains(userUpdate);
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение списка пользователей")
	void shouldReturnListUsers() {
		User userNew = createUser();
		User userAdded = userStorage.addNewUser(userNew);

		List<User> listAllUsers = new ArrayList<>(userStorage.getAllUsers());

		assertThat(listAllUsers).isNotEmpty().hasSize(1).contains(userAdded);
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Добавление друга")
	void shouldAddFriend() throws ValidationException {
		User userNew = createUser();
		User userAdded = userStorage.addNewUser(userNew);
		User userNew2 = createUser();
		User userAdded2 = userStorage.addNewUser(userNew2);

		userStorage.addFriend(1L, 2L);

		List<User> listUserFrends = new ArrayList<>(userStorage.getAllFriendsOfUser(userAdded.getId()));
		List<User> listFrendFrends = new ArrayList<>(userStorage.getAllFriendsOfUser(userAdded2.getId()));
		assertThat(listUserFrends).isNotEmpty().hasSize(1).contains(userAdded2);
		assertThat(listFrendFrends).isEmpty();
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение списка друзей пользователя")
	void shouldAllFriendsByUser() throws ValidationException {
		User userNew = createUser();
		User userAdded = userStorage.addNewUser(userNew);
		User userNew2 = createUser();
		userStorage.addNewUser(userNew2);
		userStorage.addFriend(1L, 2L);

		List<User> listAllFriendsOfUser = new ArrayList<>(userStorage.getAllFriendsOfUser(userAdded.getId()));

		assertThat(listAllFriendsOfUser).isNotEmpty().hasSize(1);
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение списка общих друзей")
	void shouldMutualFriends() throws ValidationException {
		User userNew = createUser();
		User userAdded = userStorage.addNewUser(userNew);
		User userNew2 = createUser();
		User userAdded2 = userStorage.addNewUser(userNew2);
		User userNew3 = createUser();
		userNew3.setEmail("mail3@yandex.ru");
		userNew3.setName("NameUpdate3");
		userNew3.setLogin("LoginUpdate3");
		User userAdded3 = userStorage.addNewUser(userNew3);
		userStorage.addFriend(userAdded.getId(), userAdded3.getId());
		userStorage.addFriend(userAdded2.getId(), userAdded3.getId());

		List<User> listMutualFriends = new ArrayList<>(userStorage.getMutualFriends(userAdded.getId(), userAdded2.getId()));

		assertThat(listMutualFriends).isNotEmpty().hasSize(1).contains(userAdded3);
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Удаление друга")
	void shouldDeleteFriend() throws ValidationException {
		log.info("Тест: {}", "Удаление друга");
		User userNew = createUser();
		User userAdded = userStorage.addNewUser(userNew);
		User userNew2 = createUser();
		User userAdded2 = userStorage.addNewUser(userNew2);
		User userNew3 = createUser();
		userNew3.setEmail("mail3@yandex.ru");
		userNew3.setName("NameUpdate3");
		userNew3.setLogin("LoginUpdate3");
		User userAdded3 = userStorage.addNewUser(userNew3);
		userStorage.addFriend(userAdded.getId(), userAdded3.getId());
		userStorage.addFriend(userAdded2.getId(), userAdded3.getId());

		userStorage.deleteFriend(userAdded.getId(), userAdded2.getId());
		userStorage.deleteFriend(userAdded.getId(), userAdded3.getId());


		List<User> list1AfterDeleteFriend = new ArrayList<>(userStorage.getAllFriendsOfUser(userAdded.getId()));
		List<User> list2AfterDeleteFriend = new ArrayList<>(userStorage.getAllFriendsOfUser(userAdded3.getId()));
		assertThat(list1AfterDeleteFriend).isEmpty();
		assertThat(list2AfterDeleteFriend).isEmpty();
	}

	private User createUser() {
		user = User.builder().email("mail@mail.ru").login("Login").name("Name").birthday(LocalDate.of(2022, 8, 20))
				   .build();
		return user;
	}

	private User updateUser() {
		user = User.builder().id(1L).email("mail@yandex.ru").login("LoginUpdate").name("NameUpdate")
				   .birthday(LocalDate.of(1946, 8, 20)).build();
		return user;
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Добавление нового фильма")
	void shouldAddNewFilm() {
		Film filmNew = createFilm();
		Film filmAdded = filmStorage.addNewFilm(filmNew);
		List<Film> list = new ArrayList<>(filmStorage.getAllFilms());
		assertThat(list).isNotEmpty().hasSize(1).contains(filmStorage.getFilmById(filmAdded.getId()));
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Обновление фильма ")
	void shouldUpdateFilm() {
		Film filmNew = createFilm();
		Film filmAdded = filmStorage.addNewFilm(filmNew);
		Film filmUpdate = updateFilm();

		filmStorage.updateFilm(filmUpdate);

		List<Film> listUpdate = new ArrayList<>(filmStorage.getAllFilms());
		assertThat(listUpdate).isNotEmpty().hasSize(1).contains(filmStorage.getFilmById(filmAdded.getId()));
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение списка всех фильмов")
	void shouldReturnListAllFilms() {
		Film filmNew = createFilm();
		Film filmAdded = filmStorage.addNewFilm(filmNew);
		List<Film> listAllFilms = new ArrayList<>(filmStorage.getAllFilms());
		assertThat(listAllFilms).isNotEmpty().hasSize(1).contains(filmStorage.getFilmById(filmAdded.getId()));
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Добавление лайка")
	void shouldAddLike() {
		User userNew = createUser();
		User userAdded = userStorage.addNewUser(userNew);
		Film filmNew = createFilm();
		Film filmAdded = filmStorage.addNewFilm(filmNew);
		filmStorage.addLike(filmAdded.getId(), userAdded.getId());

		List<Film> films = new ArrayList<>(filmStorage.getTopRatingFilms(10));

		assertThat(films).isNotEmpty().hasSize(1).contains(filmStorage.getFilmById(filmAdded.getId()));
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение списка лучших фильмов")
	void shouldReturnTopRatingFilms() {
		User userNew1 = createUser();
		User userAdded1 = userStorage.addNewUser(userNew1);
		User userNew2 = createUser();
		User userAdded2 = userStorage.addNewUser(userNew2);
		User userNew3 = createUser();
		User userAdded3 = userStorage.addNewUser(userNew3);
		Film filmNew1 = createFilm();
		Film filmNew2 = createFilm();
		Film filmNew3 = createFilm();
		filmNew2.setName("Name Film2");
		filmNew3.setName("Name Film3");
		Film filmAdded1 = filmStorage.addNewFilm(filmNew1);
		Film filmAdded2 = filmStorage.addNewFilm(filmNew2);
		Film filmAdded3 = filmStorage.addNewFilm(filmNew3);
		filmStorage.addLike(filmAdded1.getId(), userAdded1.getId());
		filmStorage.addLike(filmAdded1.getId(), userAdded2.getId());
		filmStorage.addLike(filmAdded1.getId(), userAdded3.getId());
		filmStorage.addLike(filmAdded2.getId(), userAdded2.getId());
		filmStorage.addLike(filmAdded2.getId(), userAdded3.getId());

		List<Film> films = new ArrayList<>(filmStorage.getTopRatingFilms(10));

		assertThat(films).isNotEmpty().hasSize(3);
		assertThat(films.get(0)).isEqualTo(filmStorage.getFilmById(filmAdded1.getId()));
		assertThat(films.get(1)).isEqualTo(filmStorage.getFilmById(filmAdded2.getId()));
		assertThat(films.get(2)).isEqualTo(filmStorage.getFilmById(filmAdded3.getId()));
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Удаление лайка")
	void shouldDeleteLike() {
		User userNew1 = createUser();
		User userAdded1 = userStorage.addNewUser(userNew1);
		Film filmNew1 = createFilm();
		Film filmAdded1 = filmStorage.addNewFilm(filmNew1);
		Film filmNew2 = createFilm();
		Film filmAdded2 = filmStorage.addNewFilm(filmNew2);
		filmStorage.addLike(filmAdded1.getId(), userAdded1.getId());
		filmStorage.addLike(filmAdded2.getId(), userAdded1.getId());

		filmStorage.deleteLike(1L, 1L);

		List<Film> films = new ArrayList<>(filmStorage.getTopRatingFilms(10));
		assertThat(films.get(0)).isEqualTo(filmStorage.getFilmById(filmAdded2.getId()));
		assertThat(films.get(1)).isEqualTo(filmStorage.getFilmById(filmAdded1.getId()));
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение списка всех жанров")
	void shouldReturnListAllGenres() {
		List<Genre> genres = new ArrayList<>(filmStorage.getAllGenres());

		assertThat(genres).isNotEmpty();
		assertThat(genres.get(0)).hasFieldOrPropertyWithValue("id", 1L).hasFieldOrPropertyWithValue("name", "Комедия");
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение жанра по ID")
	void shouldReturnGenresById() {
		Genre genre = filmStorage.getGenresById(2L);
		assertThat(genre).hasFieldOrPropertyWithValue("id", 2L).hasFieldOrPropertyWithValue("name", "Драма");
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение списка всех рейтингов МРА")
	void shouldReturnListAllRatingsMpa() {
		List<Mpa> ratings = new ArrayList<>(filmStorage.getAllRatingsMpa());

		assertThat(ratings).isNotEmpty();
		assertThat(ratings.get(0)).hasFieldOrPropertyWithValue("id", 1L).hasFieldOrPropertyWithValue("name", "G");
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение рейтинга МРА по ID")
	void shouldReturnRatingsMpaById() {
		Mpa mpa = filmStorage.getRatingsMpaById(2L);
		assertThat(mpa).hasFieldOrPropertyWithValue("id", 2L).hasFieldOrPropertyWithValue("name", "PG");
	}

	private Film createFilm() {
		film = Film.builder().name("Name Film").description("blah-blah-blah").releaseDate(LocalDate.of(2022, 8, 20))
				   .duration(120).build();
		return film;
	}

	private Film updateFilm() {
		film = Film.builder().id(1L).name("updateName Film").description("blah-blah-blah")
				   .releaseDate(LocalDate.of(2022, 5, 12)).duration(60).build();
		return film;
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Добавление нового режиссёра")
	void shouldAddNewDirector() {
		directorStorage.addNewDirector(createDirector());
		List<Director> genres = new ArrayList<>(directorStorage.getAllDirectors());

		assertThat(genres).isNotEmpty();
		assertThat(genres.get(6)).hasFieldOrPropertyWithValue("id", 7L).hasFieldOrPropertyWithValue("name", "Name Director");
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Обновление режиссёра")
	void shouldUpdateDirector() {
		Director director = createDirector();
		director.setId(1L);
		directorStorage.updateDirector(director);

		Director director2 = directorStorage.getDirectorById(1L);
		assertThat(director2).hasFieldOrPropertyWithValue("id", 1L).hasFieldOrPropertyWithValue("name", "Name Director");
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение списка всех режиссёров")
	void shouldReturnListAllDirectors() {
		List<Director> genres = new ArrayList<>(directorStorage.getAllDirectors());

		assertThat(genres).isNotEmpty();
		assertThat(genres.get(0)).hasFieldOrPropertyWithValue("id", 1L).hasFieldOrPropertyWithValue("name", "Стивен Спилберг");
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Получение режиссёра по ID")
	void shouldReturnDirectorById() {
		Director director = directorStorage.getDirectorById(2L);
		assertThat(director).hasFieldOrPropertyWithValue("id", 2L).hasFieldOrPropertyWithValue("name", "Мартин Скорсезе");
	}

	@Test
	@Sql ({"/test-schema.sql", "/data.sql"})
	@DisplayName ("Удаление режиссёра по ID")
	void shouldDeleteDirectorById() {
		directorStorage.deleteDirectorById(1L);
		List<Director> genres = new ArrayList<>(directorStorage.getAllDirectors());

		assertThat(genres).isNotEmpty();
		assertThat(genres.get(0)).hasFieldOrPropertyWithValue("id", 2L).hasFieldOrPropertyWithValue("name", "Мартин Скорсезе");
	}

	private Director createDirector() {
		return Director.builder()
					   .name("Name Director")
					   .build();
	}
}

