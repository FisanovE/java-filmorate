package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.impl.ReviewDbStorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


import java.time.LocalDate;
import java.util.*;

@Slf4j
@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final FilmService filmService;
    private final DirectorService directorService;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final ReviewService reviewService;
    private final ReviewDbStorage reviewDbStorage;
    private final EventService eventService;
    private User user;
    private Film film;
    private Review review;

    private Film createFilm() {
        film = Film.builder()
                .name("Name Film")
                .description("blah-blah-blah")
                .releaseDate(LocalDate.of(2022, 8, 20))
                .duration(120)
                .build();
        film.setGenres(new LinkedHashSet<>());
        film.setDirectors(new LinkedHashSet<>());
        return film;
    }

    private Film updateFilm() {
        film = Film.builder().id(1L)
                .name("updateName Film")
                .description("blah-blah-blah")
                .releaseDate(LocalDate.of(2022, 5, 12))
                .duration(60)
                .build();
        film.setGenres(new LinkedHashSet<>());
        film.setDirectors(new LinkedHashSet<>());
        return film;
    }

    private User createUser() {
        user = User.builder()
                .email("mail@mail.ru")
                .login("Login")
                .name("Name")
                .birthday(LocalDate.of(2022, 8, 20))
                .build();
        return user;
    }

    private User updateUser() {
        user = User.builder()
                .id(1L)
                .email("mail@yandex.ru")
                .login("LoginUpdate")
                .name("NameUpdate")
                .birthday(LocalDate.of(1946, 8, 20))
                .build();
        return user;
    }

    private Review createReview(Long userId, Long filmId) {
        review = Review.builder()
                .content("everything is very bad")
                .isPositive(false)
                .userId(userId)
                .filmId(filmId)
                .useful(10L)
                .build();
        return review;
    }

    private Review updateReview(Long userId, Long filmId) {
        review = Review.builder()
                .content("everything is very good")
                .isPositive(true)
                .userId(userId)
                .filmId(filmId)
                .useful(20L)
                .build();
        return review;
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Добавление нового пользователя")
    void shouldAddNewUser() {
        User userNew = createUser();

        User userAdded = userService.create(userNew);

        List<User> list = new ArrayList<>(userService.getAll());
        assertThat(list).isNotEmpty().hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(userAdded.getId());
        assertThat(list.get(0).getLogin()).isEqualTo(userAdded.getLogin());
        assertThat(list.get(0).getName()).isEqualTo(userAdded.getName());
        assertThat(list.get(0).getEmail()).isEqualTo(userAdded.getEmail());
        assertThat(list.get(0).getBirthday()).isEqualTo(userAdded.getBirthday());
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение пользователя по ID")
    void shouldReturnUserById() {
        User userNew = createUser();
        userService.create(userNew);

        Optional<User> userOptional = Optional.ofNullable(userService.getById(1L));

        assertThat(userOptional).isPresent()
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", 1L));
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Обновление пользователя")
    void shouldUpdateUser() {
        User userNew = createUser();
        userService.create(userNew);

        User userUpdate = updateUser();

        userService.update(userUpdate);
        List<User> listUpdate = new ArrayList<>(userService.getAll());
        assertThat(listUpdate).isNotEmpty().hasSize(1);
        assertThat(listUpdate.get(0).getId()).isEqualTo(userUpdate.getId());
        assertThat(listUpdate.get(0).getLogin()).isEqualTo(userUpdate.getLogin());
        assertThat(listUpdate.get(0).getName()).isEqualTo(userUpdate.getName());
        assertThat(listUpdate.get(0).getEmail()).isEqualTo(userUpdate.getEmail());
        assertThat(listUpdate.get(0).getBirthday()).isEqualTo(userUpdate.getBirthday());
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка пользователей")
    void shouldReturnListUsers() {
        User userNew = createUser();
        User userAdded = userService.create(userNew);

        List<User> listAllUsers = new ArrayList<>(userService.getAll());

        assertThat(listAllUsers).isNotEmpty().hasSize(1);
        assertThat(listAllUsers.get(0).getId()).isEqualTo(userAdded.getId());
        assertThat(listAllUsers.get(0).getLogin()).isEqualTo(userAdded.getLogin());
        assertThat(listAllUsers.get(0).getName()).isEqualTo(userAdded.getName());
        assertThat(listAllUsers.get(0).getEmail()).isEqualTo(userAdded.getEmail());
        assertThat(listAllUsers.get(0).getBirthday()).isEqualTo(userAdded.getBirthday());
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Добавление друга")
    void shouldAddFriend() throws ValidationException {
        User userNew = createUser();
        User userAdded = userService.create(userNew);
        User userNew2 = createUser();
        userNew2.setLogin("Login2");
        userNew2.setEmail("mail@mail.com");
        User userAdded2 = userService.create(userNew2);

        userService.addFriend(1L, 2L);

        List<User> listUserFriends = new ArrayList<>(userService.getAllFriends(userAdded.getId()));
        List<User> listFriendFriends = new ArrayList<>(userService.getAllFriends(userAdded2.getId()));
        assertThat(listUserFriends).isNotEmpty().hasSize(1);
        assertThat(listUserFriends.get(0).getId()).isEqualTo(userAdded2.getId());
        assertThat(listUserFriends.get(0).getLogin()).isEqualTo(userAdded2.getLogin());
        assertThat(listUserFriends.get(0).getName()).isEqualTo(userAdded2.getName());
        assertThat(listUserFriends.get(0).getEmail()).isEqualTo(userAdded2.getEmail());
        assertThat(listUserFriends.get(0).getBirthday()).isEqualTo(userAdded2.getBirthday());

        assertThat(listFriendFriends).isEmpty();
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка друзей пользователя")
    void shouldAllFriendsByUser() throws ValidationException {
        User userNew = createUser();
        User userAdded = userService.create(userNew);
        User userNew2 = createUser();
        userService.create(userNew2);
        userService.addFriend(1L, 2L);

        List<User> listAllFriendsOfUser = new ArrayList<>(userService.getAllFriends(userAdded.getId()));

        assertThat(listAllFriendsOfUser).isNotEmpty().hasSize(1);
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка общих друзей")
    void shouldMutualFriends() throws ValidationException {
        User userNew = createUser();
        User userAdded = userService.create(userNew);
        User userNew2 = createUser();
        User userAdded2 = userService.create(userNew2);
        User userNew3 = createUser();
        userNew3.setEmail("mail3@yandex.ru");
        userNew3.setName("NameUpdate3");
        userNew3.setLogin("LoginUpdate3");
        User userAdded3 = userService.create(userNew3);
        userService.addFriend(userAdded.getId(), userAdded3.getId());
        userService.addFriend(userAdded2.getId(), userAdded3.getId());

        List<User> listMutualFriends = new ArrayList<>(userService.getMutualFriends(userAdded.getId(), userAdded2.getId()));

        assertThat(listMutualFriends).isNotEmpty().hasSize(1);
        assertThat(listMutualFriends.get(0).getId()).isEqualTo(userAdded3.getId());
        assertThat(listMutualFriends.get(0).getLogin()).isEqualTo(userAdded3.getLogin());
        assertThat(listMutualFriends.get(0).getName()).isEqualTo(userAdded3.getName());
        assertThat(listMutualFriends.get(0).getEmail()).isEqualTo(userAdded3.getEmail());
        assertThat(listMutualFriends.get(0).getBirthday()).isEqualTo(userAdded3.getBirthday());
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Удаление друга")
    void shouldDeleteFriend() throws ValidationException {
        log.info("Тест: {}", "Удаление друга");
        User userNew = createUser();
        User userAdded = userService.create(userNew);
        User userNew2 = createUser();
        User userAdded2 = userService.create(userNew2);
        User userNew3 = createUser();
        userNew3.setEmail("mail3@yandex.ru");
        userNew3.setName("NameUpdate3");
        userNew3.setLogin("LoginUpdate3");
        User userAdded3 = userService.create(userNew3);
        userService.addFriend(userAdded.getId(), userAdded3.getId());
        userService.addFriend(userAdded2.getId(), userAdded3.getId());

        userService.deleteFriend(userAdded.getId(), userAdded2.getId());
        userService.deleteFriend(userAdded.getId(), userAdded3.getId());


        List<User> list1AfterDeleteFriend = new ArrayList<>(userService.getAllFriends(userAdded.getId()));
        List<User> list2AfterDeleteFriend = new ArrayList<>(userService.getAllFriends(userAdded3.getId()));
        assertThat(list1AfterDeleteFriend).isEmpty();
        assertThat(list2AfterDeleteFriend).isEmpty();
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Добавление нового фильма")
    void shouldAddNewFilm() {
        Film filmNew = createFilm();
        Film filmAdded = filmStorage.create(filmNew);
        List<Film> films = new ArrayList<>(filmService.getAll());
        assertThat(films).isNotEmpty().hasSize(1);
        assertThat(films.get(0).getId()).isEqualTo(filmAdded.getId());
        assertThat(films.get(0).getName()).isEqualTo(filmAdded.getName());
        assertThat(films.get(0).getDescription()).isEqualTo(filmAdded.getDescription());
        assertThat(films.get(0).getDuration()).isEqualTo(filmAdded.getDuration());
        assertThat(films.get(0).getReleaseDate()).isEqualTo(filmAdded.getReleaseDate());
        assertThat(films.get(0).getMpa()).isEqualTo(filmAdded.getMpa());
        assertThat(films.get(0).getDirectors()).isEqualTo(filmAdded.getDirectors());
        assertThat(films.get(0).getGenres()).isEqualTo(filmAdded.getGenres());
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Обновление фильма ")
    void shouldUpdateFilm() {
        Film filmNew = createFilm();
        Film filmAdded = filmService.create(filmNew);
        Film filmUpdate = updateFilm();

        Film filmUpdate2 = filmService.update(filmUpdate);

        List<Film> listUpdate = new ArrayList<>(filmService.getAll());

        assertThat(listUpdate).isNotEmpty().hasSize(1);
        assertThat(listUpdate.get(0).getId()).isEqualTo(filmUpdate2.getId());
        assertThat(listUpdate.get(0).getName()).isEqualTo(filmUpdate2.getName());
        assertThat(listUpdate.get(0).getDescription()).isEqualTo(filmUpdate2.getDescription());
        assertThat(listUpdate.get(0).getDuration()).isEqualTo(filmUpdate2.getDuration());
        assertThat(listUpdate.get(0).getReleaseDate()).isEqualTo(filmUpdate2.getReleaseDate());
        assertThat(listUpdate.get(0).getMpa()).isEqualTo(filmUpdate2.getMpa());
        assertThat(listUpdate.get(0).getDirectors()).isEqualTo(filmUpdate2.getDirectors());
        assertThat(listUpdate.get(0).getGenres()).isEqualTo(filmUpdate2.getGenres());
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка всех фильмов")
    void shouldReturnListAllFilms() {
        Film filmNew = createFilm();
        Film filmAdded = filmService.create(filmNew);
        List<Film> listAllFilms = new ArrayList<>(filmService.getAll());
        assertThat(listAllFilms).isNotEmpty().hasSize(1);
        assertThat(listAllFilms.get(0).getId()).isEqualTo(filmAdded.getId());
        assertThat(listAllFilms.get(0).getName()).isEqualTo(filmAdded.getName());
        assertThat(listAllFilms.get(0).getDescription()).isEqualTo(filmAdded.getDescription());
        assertThat(listAllFilms.get(0).getDuration()).isEqualTo(filmAdded.getDuration());
        assertThat(listAllFilms.get(0).getReleaseDate()).isEqualTo(filmAdded.getReleaseDate());
        assertThat(listAllFilms.get(0).getMpa()).isEqualTo(filmAdded.getMpa());
        assertThat(listAllFilms.get(0).getDirectors()).isEqualTo(filmAdded.getDirectors());
        assertThat(listAllFilms.get(0).getGenres()).isEqualTo(filmAdded.getGenres());
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Добавление лайка")
    void shouldAddLike() {
        User userNew = createUser();
        User userAdded = userService.create(userNew);
        Film filmNew = createFilm();
        Film filmAdded = filmService.create(filmNew);
        filmService.addLike(filmAdded.getId(), userAdded.getId());

        List<Film> films = new ArrayList<>(filmService.getTopRatingFilms(10));

        assertThat(films).isNotEmpty().hasSize(1);
        assertThat(films.get(0).getId()).isEqualTo(filmAdded.getId());
        assertThat(films.get(0).getName()).isEqualTo(filmAdded.getName());
        assertThat(films.get(0).getDescription()).isEqualTo(filmAdded.getDescription());
        assertThat(films.get(0).getDuration()).isEqualTo(filmAdded.getDuration());
        assertThat(films.get(0).getReleaseDate()).isEqualTo(filmAdded.getReleaseDate());
        assertThat(films.get(0).getMpa()).isEqualTo(filmAdded.getMpa());
        assertThat(films.get(0).getDirectors()).isEqualTo(filmAdded.getDirectors());
        assertThat(films.get(0).getGenres()).isEqualTo(filmAdded.getGenres());
    }

    /**
     * ALG_8
     */
    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка лучших фильмов")
    void shouldReturnTopRatingFilms() {
        User userNew1 = createUser();
        User userAdded1 = userService.create(userNew1);
        User userNew2 = createUser();
        User userAdded2 = userService.create(userNew2);
        User userNew3 = createUser();
        User userAdded3 = userService.create(userNew3);
        Film filmNew1 = createFilm();
        Film filmNew2 = createFilm();
        Film filmNew3 = createFilm();
        filmNew2.setName("Name Film2");
        filmNew3.setName("Name Film3");
        Film filmAdded1 = filmService.create(filmNew1);
        Film filmAdded2 = filmService.create(filmNew2);
        Film filmAdded3 = filmService.create(filmNew3);
        filmService.addLike(filmAdded1.getId(), userAdded1.getId());
        filmService.addLike(filmAdded1.getId(), userAdded2.getId());
        filmService.addLike(filmAdded1.getId(), userAdded3.getId());
        filmService.addLike(filmAdded2.getId(), userAdded2.getId());
        filmService.addLike(filmAdded2.getId(), userAdded3.getId());

        List<Film> films = new ArrayList<>(filmService.getTopRatingFilms(10));

        assertThat(films).isNotEmpty().hasSize(3);

        assertThat(films.get(0).getId()).isEqualTo(filmAdded1.getId());
        assertThat(films.get(0).getName()).isEqualTo(filmAdded1.getName());
        assertThat(films.get(0).getDescription()).isEqualTo(filmAdded1.getDescription());
        assertThat(films.get(0).getDuration()).isEqualTo(filmAdded1.getDuration());
        assertThat(films.get(0).getReleaseDate()).isEqualTo(filmAdded1.getReleaseDate());
        assertThat(films.get(0).getMpa()).isEqualTo(filmAdded1.getMpa());
        assertThat(films.get(0).getDirectors()).isEqualTo(filmAdded1.getDirectors());
        assertThat(films.get(0).getGenres()).isEqualTo(filmAdded1.getGenres());

        assertThat(films.get(1).getId()).isEqualTo(filmAdded2.getId());
        assertThat(films.get(1).getName()).isEqualTo(filmAdded2.getName());
        assertThat(films.get(1).getDescription()).isEqualTo(filmAdded2.getDescription());
        assertThat(films.get(1).getDuration()).isEqualTo(filmAdded2.getDuration());
        assertThat(films.get(1).getReleaseDate()).isEqualTo(filmAdded2.getReleaseDate());
        assertThat(films.get(1).getMpa()).isEqualTo(filmAdded2.getMpa());
        assertThat(films.get(1).getDirectors()).isEqualTo(filmAdded2.getDirectors());
        assertThat(films.get(1).getGenres()).isEqualTo(filmAdded2.getGenres());

        assertThat(films.get(2).getId()).isEqualTo(filmAdded3.getId());
        assertThat(films.get(2).getName()).isEqualTo(filmAdded3.getName());
        assertThat(films.get(2).getDescription()).isEqualTo(filmAdded3.getDescription());
        assertThat(films.get(2).getDuration()).isEqualTo(filmAdded3.getDuration());
        assertThat(films.get(2).getReleaseDate()).isEqualTo(filmAdded3.getReleaseDate());
        assertThat(films.get(2).getMpa()).isEqualTo(filmAdded3.getMpa());
        assertThat(films.get(2).getDirectors()).isEqualTo(filmAdded3.getDirectors());
        assertThat(films.get(2).getGenres()).isEqualTo(filmAdded3.getGenres());

        Film filmNew4 = createFilm();
        filmNew4.setName("Name Film4");
        filmNew4.setGenres(new LinkedHashSet<>(List.of(Genre.builder().id(1L).name("Комедия").build())));
        Film filmAdded4 = filmService.create(filmNew4);

        List<Film> filmsByGenre = new ArrayList<>(filmService.getTopRatingFilmsByGenreAndYear(10, 1L, -1));

        assertThat(filmsByGenre).isNotEmpty().hasSize(1);
        assertThat(filmsByGenre.get(0).getId()).isEqualTo(filmAdded4.getId());
        assertThat(filmsByGenre.get(0).getName()).isEqualTo(filmAdded4.getName());
        assertThat(filmsByGenre.get(0).getDescription()).isEqualTo(filmAdded4.getDescription());
        assertThat(filmsByGenre.get(0).getDuration()).isEqualTo(filmAdded4.getDuration());
        assertThat(filmsByGenre.get(0).getReleaseDate()).isEqualTo(filmAdded4.getReleaseDate());
        assertThat(filmsByGenre.get(0).getMpa()).isEqualTo(filmAdded4.getMpa());
        assertThat(filmsByGenre.get(0).getDirectors()).isEqualTo(filmAdded4.getDirectors());
        assertThat(filmsByGenre.get(0).getGenres()).isEqualTo(filmAdded4.getGenres());

        Film filmNew5 = createFilm();
        filmNew5.setName("Name Film5");
        filmNew5.setReleaseDate(LocalDate.of(2020, 01, 01));
        Film filmAdded5 = filmService.create(filmNew5);

        List<Film> filmsByYear = new ArrayList<>(filmService.getTopRatingFilmsByGenreAndYear(10, -1, 2020));

        assertThat(filmsByYear).isNotEmpty().hasSize(1);
        assertThat(filmsByYear.get(0).getId()).isEqualTo(filmAdded5.getId());
        assertThat(filmsByYear.get(0).getName()).isEqualTo(filmAdded5.getName());
        assertThat(filmsByYear.get(0).getDescription()).isEqualTo(filmAdded5.getDescription());
        assertThat(filmsByYear.get(0).getDuration()).isEqualTo(filmAdded5.getDuration());
        assertThat(filmsByYear.get(0).getReleaseDate()).isEqualTo(filmAdded5.getReleaseDate());
        assertThat(filmsByYear.get(0).getMpa()).isEqualTo(filmAdded5.getMpa());
        assertThat(filmsByYear.get(0).getDirectors()).isEqualTo(filmAdded5.getDirectors());
        assertThat(filmsByYear.get(0).getGenres()).isEqualTo(filmAdded5.getGenres());

        Film filmNew6 = createFilm();
        filmNew6.setName("Name Film5");
        filmNew6.setReleaseDate(LocalDate.of(2021, 01, 01));
        filmNew6.setGenres(new LinkedHashSet<>(List.of(Genre.builder().id(2L).name("Драма").build())));
        Film filmAdded6 = filmService.create(filmNew6);

        List<Film> filmsByGenreAndYear = new ArrayList<>(filmService.getTopRatingFilmsByGenreAndYear(10, 2L, 2021));

        assertThat(filmsByGenreAndYear).isNotEmpty().hasSize(1);
        assertThat(filmsByGenreAndYear.get(0).getId()).isEqualTo(filmAdded6.getId());
        assertThat(filmsByGenreAndYear.get(0).getName()).isEqualTo(filmAdded6.getName());
        assertThat(filmsByGenreAndYear.get(0).getDescription()).isEqualTo(filmAdded6.getDescription());
        assertThat(filmsByGenreAndYear.get(0).getDuration()).isEqualTo(filmAdded6.getDuration());
        assertThat(filmsByGenreAndYear.get(0).getReleaseDate()).isEqualTo(filmAdded6.getReleaseDate());
        assertThat(filmsByGenreAndYear.get(0).getMpa()).isEqualTo(filmAdded6.getMpa());
        assertThat(filmsByGenreAndYear.get(0).getDirectors()).isEqualTo(filmAdded6.getDirectors());
        assertThat(filmsByGenreAndYear.get(0).getGenres()).isEqualTo(filmAdded6.getGenres());
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Удаление лайка")
    void shouldDeleteLike() {
        User userNew1 = createUser();
        User userAdded1 = userService.create(userNew1);
        Film filmNew1 = createFilm();
        Film filmAdded1 = filmService.create(filmNew1);
        Film filmNew2 = createFilm();
        Film filmAdded2 = filmService.create(filmNew2);
        filmService.addLike(filmAdded1.getId(), userAdded1.getId());
        filmService.addLike(filmAdded2.getId(), userAdded1.getId());

        filmService.deleteLike(filmAdded1.getId(), userAdded1.getId());

        List<Film> films = new ArrayList<>(filmService.getTopRatingFilms(10));
        assertThat(films.get(0).getId()).isEqualTo(filmAdded2.getId());
        assertThat(films.get(1).getId()).isEqualTo(filmAdded1.getId());
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка всех жанров")
    void shouldReturnListAllGenres() {
        List<Genre> genres = new ArrayList<>(genreService.getAll());

        assertThat(genres).isNotEmpty();
        assertThat(genres.get(0)).hasFieldOrPropertyWithValue("id", 1L).hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    @Sql({"/schema.sql", "/data.sql"})
    @DisplayName("Получение жанра по ID")
    void shouldReturnGenresById() {
        Genre genre = genreService.getById(2L);
        assertThat(genre).hasFieldOrPropertyWithValue("id", 2L).hasFieldOrPropertyWithValue("name", "Драма");
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка всех рейтингов МРА")
    void shouldReturnListAllRatingsMpa() {
        List<Mpa> ratings = new ArrayList<>(mpaService.getAll());

        assertThat(ratings).isNotEmpty();
        assertThat(ratings.get(0)).hasFieldOrPropertyWithValue("id", 1L).hasFieldOrPropertyWithValue("name", "G");
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение рейтинга МРА по ID")
    void shouldReturnRatingsMpaById() {
        Mpa mpa = mpaService.getById(2L);
        assertThat(mpa).hasFieldOrPropertyWithValue("id", 2L).hasFieldOrPropertyWithValue("name", "PG");
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Тестирование отзывов")
    void reviewDbStorageTest() {
        User user = createUser();
        User user2 = createUser();
        userService.create(user);
        userService.create(user2);
        Film film = createFilm();
        Film dbFilm = filmService.create(film);
        Review reviewMain = Review.builder()
                .content("True")
                .isPositive(true)
                .userId(user.getId())
                .filmId(film.getId())
                .build();

        Review addedReview = reviewDbStorage.create(reviewMain);
        reviewMain.setContent("False");
        reviewMain.setIsPositive(false);
        reviewDbStorage.update(reviewMain);
        Review reviewInDb = reviewService.getById(1L);
        List<Review> reviews = reviewService.getByFilmId(dbFilm.getId(), 1);
        reviewService.addLike(1L, 1L);
        reviewService.addDislike(1L, 2L);
        reviewService.deleteLike(1L, 1L);
        reviewService.deleteDislike(1L, 2L);

        assertAll("Отзывы работают не правильно: ",
                () -> assertEquals(addedReview.getReviewId(), 1L, "addNewReview работает не правильно"),
                () -> assertNotNull(reviewInDb, "getReviewById работает не правильно"),
                () -> assertEquals(reviewInDb.getContent(), "False", "updateReview работает не правильно"),
                () -> assertEquals(reviews.size(), 1, "getByFilmId работает не правильно"),
                () -> assertEquals(reviewInDb.getUseful(), 0, "Оценка отзыва работает не правильно")
        );
    }


    /**
     * ALG_7
     */
    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Добавление нового режиссёра")
    void shouldAddNewDirector() {
        Director director = directorService.create(createDirector());
        List<Director> genres = new ArrayList<>(directorService.getAll());

        assertThat(genres).isNotEmpty();
        assertThat(genres.get(0)).hasFieldOrPropertyWithValue("id", director.getId())
                .hasFieldOrPropertyWithValue("name", "Name Director");
    }

    /**
     * ALG_7
     */
    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Обновление режиссёра")
    void shouldUpdateDirector() {
        Director director = createDirector();
        Director addedDirector = directorService.create(director);
        addedDirector.setName("UpdateDirector");
        directorService.update(addedDirector);

        Director director2 = directorService.getById(addedDirector.getId());
        assertThat(director2).hasFieldOrPropertyWithValue("id", addedDirector.getId())
                .hasFieldOrPropertyWithValue("name", addedDirector.getName());
    }

    /**
     * ALG_7
     */
    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка всех режиссёров")
    void shouldReturnListAllDirectors() {
        Director director = directorService.create(Director.builder().id(1L).name("NewDirector").build());

        List<Director> allDirectors = new ArrayList<>(directorService.getAll());

        assertThat(allDirectors).isNotEmpty();
        assertThat(allDirectors.get(0)).hasFieldOrPropertyWithValue("id", director.getId())
                .hasFieldOrPropertyWithValue("name", director.getName());
    }

    /**
     * ALG_7
     */
    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение режиссёра по ID")
    void shouldReturnDirectorById() {
        Director director = createDirector();
        Director directorAdded = directorService.create(director);
        Director directorReturn = directorService.getById(directorAdded.getId());
        assertThat(directorReturn).hasFieldOrPropertyWithValue("id", directorAdded.getId())
                .hasFieldOrPropertyWithValue("name", directorAdded.getName());
    }

    /**
     * ALG_7
     */
    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Удаление режиссёра по ID")
    void shouldDeleteDirectorById() {
        Director director = createDirector();
        Director directorAdded = directorService.create(director);
        directorService.delete(directorAdded.getId());
        List<Director> directors = new ArrayList<>(directorService.getAll());

        assertThat(directors).isEmpty();
    }

    /**
     * ALG_7
     */
    private Director createDirector() {
        return Director.builder().name("Name Director").build();
    }

    /**
     * ALG_4
     */
    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Тестирование рекомендаций фильмов")
    void testRecommendations() {
        List<String> filmTitles = List.of("Матрица", "Аватар", "Властелин Колец", "Фауст", "Берсерк", "Зубастики",
                "Горизонт событий");
        List<String> userNames = List.of("Яков", "Айзек", "Платон", "Смит");

        List<Film> films = new ArrayList<>();
        Film film;
        User user;
        for (long i = 1; i <= filmTitles.size(); i++) {
            film = createFilm();
            film.setName(filmTitles.get((int) i - 1));
            filmService.create(film);

            film.setId(i);
            films.add(film);
        }
        for (String name : userNames) {
            user = createUser();
            user.setName(name);
            userService.create(user);
        }
        filmService.addLike(1L, 1L);
        filmService.addLike(2L, 1L);
        filmService.addLike(2L, 2L);
        filmService.addLike(3L, 2L);
        filmService.addLike(1L, 3L);
        filmService.addLike(2L, 3L);
        filmService.addLike(4L, 3L);
        filmService.addLike(5L, 3L);
        filmService.addLike(6L, 3L);
        filmService.addLike(7L, 4L);

        List<Film> filmsRecommendations = new ArrayList<>(filmService.getRecommendationsForUser(1L));
        films = List.of(films.get(2), films.get(3), films.get(4), films.get(5));
        assertThat(filmsRecommendations.get(0).getId()).isEqualTo(films.get(0).getId());
        assertThat(filmsRecommendations.get(1).getId()).isEqualTo(films.get(1).getId());
        assertThat(filmsRecommendations.get(2).getId()).isEqualTo(films.get(2).getId());
        assertThat(filmsRecommendations.get(3).getId()).isEqualTo(films.get(3).getId());
    }

    /**
     * ALG_7
     */
    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка фильмов режиссёра по лайкам или годам")
    public void shouldReturnAllFilmsByDirector() {
        User userNew1 = createUser();
        User userNew2 = createUser();
        User userNew3 = createUser();
        Film filmNew1 = createFilm();
        Film filmNew2 = createFilm();
        Film filmNew3 = createFilm();
        Director director = createDirector();
        User userAdded1 = userService.create(userNew1);
        User userAdded2 = userService.create(userNew2);
        User userAdded3 = userService.create(userNew3);
        Director directorAdded = directorService.create(director);
        filmNew2.setName("Name Film2");
        filmNew3.setName("Name Film3");
        filmNew2.setReleaseDate(LocalDate.parse("2021-08-20"));
        filmNew3.setReleaseDate(LocalDate.parse("2020-08-20"));
        LinkedHashSet<Director> set1 = new LinkedHashSet<>();
        set1.add(directorAdded);
        filmNew1.setDirectors(set1);
        filmNew2.setDirectors(set1);
        filmNew3.setDirectors(set1);
        filmNew1.setGenres(new LinkedHashSet<>());
        filmNew2.setGenres(new LinkedHashSet<>());
        filmNew3.setGenres(new LinkedHashSet<>());
        Film filmAdded1 = filmService.create(filmNew1);
        Film filmAdded2 = filmService.create(filmNew2);
        Film filmAdded3 = filmService.create(filmNew3);
        filmService.addLike(filmAdded1.getId(), userAdded1.getId());
        filmService.addLike(filmAdded1.getId(), userAdded2.getId());
        filmService.addLike(filmAdded1.getId(), userAdded3.getId());
        filmService.addLike(filmAdded2.getId(), userAdded1.getId());
        filmService.addLike(filmAdded2.getId(), userAdded2.getId());
        filmService.addLike(filmAdded3.getId(), userAdded1.getId());

        List<Film> filmsSortByLikes = new ArrayList<>(filmService.getAllFilmsByDirector(directorAdded.getId(), SortParameter.LIKES));
        List<Film> filmsSortByYear = new ArrayList<>(filmService.getAllFilmsByDirector(directorAdded.getId(), SortParameter.YEAR));

        assertThat(filmsSortByLikes).isNotEmpty().hasSize(3);
        assertThat(filmsSortByLikes.get(0).getId()).isEqualTo(filmAdded1.getId());
        assertThat(filmsSortByLikes.get(1).getId()).isEqualTo(filmAdded2.getId());
        assertThat(filmsSortByLikes.get(2).getId()).isEqualTo(filmAdded3.getId());

        assertThat(filmsSortByYear).isNotEmpty().hasSize(3);
        assertThat(filmsSortByYear.get(0).getId()).isEqualTo(filmAdded3.getId());
        assertThat(filmsSortByYear.get(1).getId()).isEqualTo(filmAdded2.getId());
        assertThat(filmsSortByYear.get(2).getId()).isEqualTo(filmAdded1.getId());
    }


    /**
     * ALG_7
     */
    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Поиск фильмов по режиссёру и/или названию")
    public void shouldSearchFilmsByDirectorAndName() {
        User userNew1 = createUser();
        User userNew2 = createUser();
        User userNew3 = createUser();
        User userAdded1 = userService.create(userNew1);
        User userAdded2 = userService.create(userNew2);
        User userAdded3 = userService.create(userNew3);
        Film filmNew1 = createFilm();
        Film filmNew2 = createFilm();
        Film filmNew3 = createFilm();
        Director director1 = createDirector();
        Director director2 = createDirector();
        director1.setName("David Lynch");
        director2.setName("John Cassavetes");
        Director directorAdded1 = directorService.create(director1);
        Director directorAdded2 = directorService.create(director2);
        filmNew1.setGenres(new LinkedHashSet<>());
        filmNew2.setGenres(new LinkedHashSet<>());
        filmNew3.setGenres(new LinkedHashSet<>());
        filmNew2.setName("Avatar");
        filmNew3.setName("Avangard");
        LinkedHashSet<Director> set1 = new LinkedHashSet<>();
        set1.add(directorAdded1);
        filmNew1.setDirectors(set1);
        LinkedHashSet<Director> set2 = new LinkedHashSet<>();
        set2.add(directorAdded2);
        filmNew3.setDirectors(set2);
        filmNew2.setDirectors(new LinkedHashSet<>());
        Film filmAdded1 = filmService.create(filmNew1);
        Film filmAdded2 = filmService.create(filmNew2);
        Film filmAdded3 = filmService.create(filmNew3);
        filmService.addLike(filmAdded1.getId(), userAdded1.getId());
        filmService.addLike(filmAdded1.getId(), userAdded2.getId());
        filmService.addLike(filmAdded1.getId(), userAdded3.getId());
        filmService.addLike(filmAdded3.getId(), userAdded1.getId());
        filmService.addLike(filmAdded3.getId(), userAdded2.getId());
        filmService.addLike(filmAdded2.getId(), userAdded1.getId());

        List<Film> filmsSearchByDirector = new ArrayList<>(filmService.searchFilms("aV", new ArrayList<SearchParameter>(Arrays.asList(SearchParameter.DIRECTOR))));
        List<Film> filmsSearchByTitle = new ArrayList<>(filmService.searchFilms("aV", new ArrayList<SearchParameter>(Arrays.asList(SearchParameter.TITLE))));
        List<Film> filmsSearchByTitleAndDirector = new ArrayList<>(filmService.searchFilms("aV", new ArrayList<SearchParameter>(Arrays.asList(SearchParameter.TITLE, SearchParameter.DIRECTOR))));
        List<Film> filmsSearchByDirectorAndTitle = new ArrayList<>(filmService.searchFilms("aV", new ArrayList<SearchParameter>(Arrays.asList(SearchParameter.DIRECTOR, SearchParameter.TITLE))));

        assertThat(filmsSearchByDirector).isNotEmpty().hasSize(2);
        assertThat(filmsSearchByDirector.get(0).getId()).isEqualTo(filmAdded3.getId());
        assertThat(filmsSearchByDirector.get(1).getId()).isEqualTo(filmAdded1.getId());

        assertThat(filmsSearchByTitle).isNotEmpty().hasSize(2);
        assertThat(filmsSearchByTitle.get(0).getId()).isEqualTo(filmAdded2.getId());
        assertThat(filmsSearchByTitle.get(1).getId()).isEqualTo(filmAdded3.getId());

        assertThat(filmsSearchByTitleAndDirector).isNotEmpty().hasSize(3);
        assertThat(filmsSearchByTitleAndDirector.get(0).getId()).isEqualTo(filmAdded1.getId());
        assertThat(filmsSearchByTitleAndDirector.get(1).getId()).isEqualTo(filmAdded3.getId());
        assertThat(filmsSearchByTitleAndDirector.get(2).getId()).isEqualTo(filmAdded2.getId());

        assertThat(filmsSearchByDirectorAndTitle).isNotEmpty().hasSize(3);
        assertThat(filmsSearchByDirectorAndTitle.get(0).getId()).isEqualTo(filmAdded1.getId());
        assertThat(filmsSearchByDirectorAndTitle.get(1).getId()).isEqualTo(filmAdded3.getId());
        assertThat(filmsSearchByDirectorAndTitle.get(2).getId()).isEqualTo(filmAdded2.getId());
            }


    /**
     * ALG_3
     */
    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка общих фильмов")
    public void shouldReturnCommonFilms() {
        User userNew1 = createUser();
        User userNew2 = createUser();
        User userNew3 = createUser();
        User userAdded1 = userService.create(userNew1);
        User userAdded2 = userService.create(userNew2);
        User userAdded3 = userService.create(userNew3);
        Film filmNew1 = createFilm();
        Film filmNew2 = createFilm();
        Film filmNew3 = createFilm();
        Film filmNew4 = createFilm();
        Film filmNew5 = createFilm();
        filmNew2.setName("Name Film2");
        filmNew3.setName("Name Film3");
        filmNew4.setName("Name Film4");
        filmNew5.setName("Name Film5");
        filmNew1.setGenres(new LinkedHashSet<>());
        filmNew2.setGenres(new LinkedHashSet<>());
        filmNew3.setGenres(new LinkedHashSet<>());
        filmNew4.setGenres(new LinkedHashSet<>());
        filmNew5.setGenres(new LinkedHashSet<>());
        filmNew1.setDirectors(new LinkedHashSet<>());
        filmNew2.setDirectors(new LinkedHashSet<>());
        filmNew3.setDirectors(new LinkedHashSet<>());
        filmNew4.setDirectors(new LinkedHashSet<>());
        filmNew5.setDirectors(new LinkedHashSet<>());
        Film filmAdded1 = filmService.create(filmNew1);
        Film filmAdded2 = filmService.create(filmNew2);
        Film filmAdded3 = filmService.create(filmNew3);
        Film filmAdded4 = filmService.create(filmNew4);
        Film filmAdded5 = filmService.create(filmNew5);
        filmService.addLike(filmAdded4.getId(), userAdded1.getId());
        filmService.addLike(filmAdded4.getId(), userAdded2.getId());
        filmService.addLike(filmAdded4.getId(), userAdded3.getId());
        filmService.addLike(filmAdded3.getId(), userAdded1.getId());
        filmService.addLike(filmAdded3.getId(), userAdded2.getId());
        filmService.addLike(filmAdded2.getId(), userAdded1.getId());
        filmService.addLike(filmAdded2.getId(), userAdded2.getId());
        filmService.addLike(filmAdded1.getId(), userAdded2.getId());
        filmService.addLike(filmAdded5.getId(), userAdded3.getId());
        userService.addFriend(1L, 2L);

        List<Film> films = new ArrayList<>(filmService.getCommonFilms(1L, 2L));

        assertThat(films).isNotEmpty().hasSize(3);
        assertThat(films.get(0).getId()).isEqualTo(filmAdded4.getId());
        assertThat(films.get(0).getName()).isEqualTo(filmAdded4.getName());
        assertThat(films.get(0).getDescription()).isEqualTo(filmAdded4.getDescription());
        assertThat(films.get(0).getDuration()).isEqualTo(filmAdded4.getDuration());
        assertThat(films.get(0).getReleaseDate()).isEqualTo(filmAdded4.getReleaseDate());
        assertThat(films.get(0).getMpa()).isEqualTo(filmAdded4.getMpa());
        assertThat(films.get(0).getDirectors()).isEqualTo(filmAdded4.getDirectors());
        assertThat(films.get(0).getGenres()).isEqualTo(filmAdded4.getGenres());

        assertThat(films.get(1).getId()).isEqualTo(filmAdded2.getId());
        assertThat(films.get(1).getName()).isEqualTo(filmAdded2.getName());
        assertThat(films.get(1).getDescription()).isEqualTo(filmAdded2.getDescription());
        assertThat(films.get(1).getDuration()).isEqualTo(filmAdded2.getDuration());
        assertThat(films.get(1).getReleaseDate()).isEqualTo(filmAdded2.getReleaseDate());
        assertThat(films.get(1).getMpa()).isEqualTo(filmAdded2.getMpa());
        assertThat(films.get(1).getDirectors()).isEqualTo(filmAdded2.getDirectors());
        assertThat(films.get(1).getGenres()).isEqualTo(filmAdded2.getGenres());

        assertThat(films.get(2).getId()).isEqualTo(filmAdded3.getId());
        assertThat(films.get(2).getName()).isEqualTo(filmAdded3.getName());
        assertThat(films.get(2).getDescription()).isEqualTo(filmAdded3.getDescription());
        assertThat(films.get(2).getDuration()).isEqualTo(filmAdded3.getDuration());
        assertThat(films.get(2).getReleaseDate()).isEqualTo(filmAdded3.getReleaseDate());
        assertThat(films.get(2).getMpa()).isEqualTo(filmAdded3.getMpa());
        assertThat(films.get(2).getDirectors()).isEqualTo(filmAdded3.getDirectors());
        assertThat(films.get(2).getGenres()).isEqualTo(filmAdded3.getGenres());
    }

    @Test
    @Sql({"/drop-tables.sql", "/schema.sql", "/data.sql"})
    @DisplayName("Получение списка событий")
    public void shoulReturnListEvents() {
        User userNew1 = createUser();
        User userNew2 = createUser();
        User userAdded1 = userService.create(userNew1);
        User userAdded2 = userService.create(userNew2);
        Film filmNew1 = createFilm();
        Film filmAdded1 = filmService.create(filmNew1);
        filmService.addLike(filmAdded1.getId(), userAdded1.getId());
        filmService.deleteLike(filmAdded1.getId(), userAdded1.getId());
        userService.addFriend(userAdded1.getId(), userAdded2.getId());
        userService.deleteFriend(userAdded1.getId(), userAdded2.getId());
        Review reviewNew = createReview(userAdded1.getId(), filmAdded1.getId());
        Review reviewUpdate = updateReview(userAdded1.getId(), filmAdded1.getId());
        Review reviewAdded1 = reviewService.create(reviewNew);
        reviewUpdate.setReviewId(reviewAdded1.getReviewId());
        reviewService.update(reviewUpdate);
        reviewService.delete(reviewUpdate.getReviewId());

        List<Event> events = new ArrayList<>(eventService.getEvents(userNew1.getId()));

        assertThat(events).isNotEmpty().hasSize(8);
        assertThat(events.get(0).getEventId()).isEqualTo(1L);
        assertThat(events.get(0).getUserId()).isEqualTo(1L);
        assertThat(events.get(0).getEventType()).isEqualTo("LIKE");
        assertThat(events.get(0).getOperation()).isEqualTo("ADD");
        assertThat(events.get(0).getEntityId()).isEqualTo(1L);
        assertTrue(events.get(0).getTimestamp() > 1670590017281L);

        assertThat(events.get(1).getEventId()).isEqualTo(2L);
        assertThat(events.get(1).getUserId()).isEqualTo(1L);
        assertThat(events.get(1).getEventType()).isEqualTo("LIKE");
        assertThat(events.get(1).getOperation()).isEqualTo("REMOVE");
        assertThat(events.get(1).getEntityId()).isEqualTo(1L);
        assertTrue(events.get(1).getTimestamp() > 1670590017281L);

        assertThat(events.get(2).getEventId()).isEqualTo(3L);
        assertThat(events.get(2).getUserId()).isEqualTo(1L);
        assertThat(events.get(2).getEventType()).isEqualTo("FRIEND");
        assertThat(events.get(2).getOperation()).isEqualTo("ADD");
        assertThat(events.get(2).getEntityId()).isEqualTo(2L);
        assertTrue(events.get(2).getTimestamp() > 1670590017281L);

        assertThat(events.get(3).getEventId()).isEqualTo(4L);
        assertThat(events.get(3).getUserId()).isEqualTo(1L);
        assertThat(events.get(3).getEventType()).isEqualTo("FRIEND");
        assertThat(events.get(3).getOperation()).isEqualTo("REMOVE");
        assertThat(events.get(3).getEntityId()).isEqualTo(2L);
        assertTrue(events.get(3).getTimestamp() > 1670590017281L);

        assertThat(events.get(4).getEventId()).isEqualTo(5L);
        assertThat(events.get(4).getUserId()).isEqualTo(1L);
        assertThat(events.get(4).getEventType()).isEqualTo("REVIEW");
        assertThat(events.get(4).getOperation()).isEqualTo("ADD");
        assertThat(events.get(4).getEntityId()).isEqualTo(1L);
        assertTrue(events.get(4).getTimestamp() > 1670590017281L);

        assertThat(events.get(5).getEventId()).isEqualTo(6L);
        assertThat(events.get(5).getUserId()).isEqualTo(1L);
        assertThat(events.get(5).getEventType()).isEqualTo("REVIEW");
        assertThat(events.get(5).getOperation()).isEqualTo("UPDATE");
        assertThat(events.get(5).getEntityId()).isEqualTo(1L);
        assertTrue(events.get(5).getTimestamp() > 1670590017281L);

        assertThat(events.get(6).getEventId()).isEqualTo(7L);
        assertThat(events.get(6).getUserId()).isEqualTo(1L);
        assertThat(events.get(6).getEventType()).isEqualTo("REVIEW");
        assertThat(events.get(6).getOperation()).isEqualTo("UPDATE");
        assertThat(events.get(6).getEntityId()).isEqualTo(1L);
        assertTrue(events.get(6).getTimestamp() > 1670590017281L);
    }

}