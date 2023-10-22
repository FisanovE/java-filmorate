package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.utils.DateUtils;

import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ValidateService {
    private static JdbcTemplate jdbcTemplate;

    @Autowired
    public ValidateService(JdbcTemplate jdbcTemplate) {
        ValidateService.jdbcTemplate = jdbcTemplate;
    }

    public void checkIdNotNull(Long id) throws ValidationException {
        if (id == null) {
            throw new ValidationException("Id is Null");
        }
    }

    public void checkNameNotBlank(String name) throws ValidationException {
        if (name.isBlank()) {
            throw new ValidationException("Name is empty: \"" + name + "\"");
        }
    }

    public void checkingFilmForValid(Film film) throws ValidationException {
        if (film.getName().isBlank()) {
            throw new ValidationException("Invalid title format: \"" + film.getName() + "\"");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("The maximum description length is 200 characters, you have: \"" + film.getDescription()
                    .length() + "\" characters");
        }
        if (film.getReleaseDate().isBefore(LocalDate.parse("1895-12-28", DateUtils.formatter))) {
            throw new ValidationException("Movie release date should not be earlier than 1895.12.28, you have: \"" + film.getReleaseDate() + "\"");
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("The duration of the film should be positive, you have:  \"" + film.getDuration());
        }
    }

    public void checkingUserForValid(User user) throws ValidationException {
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

    public void checkReview(Review review) {
        if (review == null) throw new ValidationException("Review must not be empty");
        if (review.getContent() == null) throw new ValidationException("Review content must not be empty");
        if (review.getIsPositive() == null) throw new ValidationException("Type review must not be empty");
        if (review.getUserId() == null) throw new ValidationException("Not valid user id");
        if (review.getFilmId() == null) throw new ValidationException("Not valid film id");
    }

    public void checkContainsUserInDatabase(Long id) {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE user_id = ?", id);
        if (sqlRows.first()) {
            log.info("User found: {}", id);
        } else {
            log.info("User not found: {}", id);
            throw new NotFoundException("User not found: " + id);
        }
    }

    public void checkContainsFilmInDatabase(Long id) {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE film_id = ?", id);
        if (sqlRows.first()) {
            log.info("Film found: {}", id);
        } else {
            log.info("Film not found: {}", id);
            throw new NotFoundException("Film not found: " + id);
        }
    }

    public void checkContainsDirectorInDatabase(Long id) {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("SELECT * FROM directors WHERE director_id = ?", id);
        if (sqlRows.first()) {
            log.info("Director found: {}", id);
        } else {
            log.info("Director not found: {}", id);
            throw new NotFoundException("Director not found: " + id);
        }
    }

    public void checkContainsGenreInDatabase(Long id) {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE genre_id = ?", id);
        if (sqlRows.first()) {
            log.info("Genre found: {}", id);
        } else {
            log.info("Genre not found: {}", id);
            throw new NotFoundException("Genre not found: " + id);
        }
    }

    public void checkContainsMpaInDatabase(Long id) {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("SELECT * FROM mpa WHERE mpa_id = ?", id);
        if (sqlRows.first()) {
            log.info("Mpa found: {}", id);
        } else {
            log.info("Mpa not found: {}", id);
            throw new NotFoundException("Mpa not found: " + id);
        }
    }

    public void checkContainsReviewInDatabase(Long id) {
        SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("SELECT * FROM reviews WHERE review_id = ?", id);
        if (sqlRows.first()) {
            log.info("Review found: {}", id);
        } else {
            log.info("Review not found: {}", id);
            throw new NotFoundException("Review not found: " + id);
        }
    }

    public void checkMatchingIdUsers(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new IllegalArgumentException("ALG_3. UserId and friendId must not be the same: " + userId + "=" + friendId);
        }
    }

}
