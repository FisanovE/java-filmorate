package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmDbStorage implements FilmStorage {

	private final JdbcTemplate jdbcTemplate;
	DirectorStorage directorStorage;

	@Autowired
	public FilmDbStorage(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Film addNewFilm(Film film) {
		String sqlRequest = "INSERT INTO films (name, description, release_date, duration) VALUES (?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, film.getName());
			ps.setString(2, film.getDescription());
			ps.setDate(3, Date.valueOf(film.getReleaseDate()));
			ps.setInt(4, film.getDuration());
			return ps;
		}, keyHolder);

		Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
		film.setId(generatedId);

		setMpaInDataBase(film);
		setGenreInDataBase(film);
		setDirectorInDataBase(film);


		log.info("Film added: {} {}", film.getId(), film.getName());
		return film;
	}

	@Override
	public Film updateFilm(Film film) {
		try {
			String sqlRequest = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ? " + "WHERE film_id = ?";
			jdbcTemplate.update(sqlRequest, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getId());
			setMpaInDataBase(film);
			setGenreInDataBase(film);
			film.setGenres(getGenresFromDataBase(film.getId()));
			setDirectorInDataBase(film);
			log.info("Film update: {} {}", film.getId(), film.getName());
			return film;
		} catch (DataIntegrityViolationException e) {
			throw new NotFoundException("Invalid Film ID:  " + film.getId());
		}
	}

	@Override
	public Film getFilmById(Long id) {
		try {
			String sql = "SELECT * FROM films WHERE film_id = ?";
			Film film = jdbcTemplate.queryForObject(sql, new FilmRowMapper(), id);
			film.setMpa(getMpaFromDataBase(id));
			film.setGenres(getGenresFromDataBase(id));
			film.setDirectors(getDirectorsFromDataBase(id));
			log.info("Film found: {} {}", id, film.getName());
			return film;
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException("Invalid Film ID: " + id);
		}
	}

	@Override
	public Collection<Film> getAllFilms() {
		String sql = "SELECT * FROM films ORDER BY film_id";
		List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());

		for (Film film : films) {
			film.setMpa(getMpaFromDataBase(film.getId()));
			film.setGenres(getGenresFromDataBase(film.getId()));
			film.setDirectors(getDirectorsFromDataBase(film.getId()));
		}

		return films;
	}

	private void setMpaInDataBase(Film film) {
		String sqlDeleteMpa = "DELETE FROM films_mpa WHERE film_id = ?";
		jdbcTemplate.update(sqlDeleteMpa, film.getId());

		if (film.getMpa() != null) {
			String sqlRatings = "INSERT INTO films_mpa (film_id, mpa_id) VALUES (?, ?)";
			jdbcTemplate.update(sqlRatings, film.getId(), film.getMpa().getId());
		}
	}

	private void setGenreInDataBase(Film film) {
		String sqlDeleteGenre = "DELETE FROM films_genres WHERE film_id = ?";
		jdbcTemplate.update(sqlDeleteGenre, film.getId());

		if (film.getGenres() != null && !film.getGenres().isEmpty()) {
			List<Genre> list = film.getGenres().stream().distinct().collect(Collectors.toList());
			film.setGenres(list);
			for (Genre genre : list) {
				String sqlGenres = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";
				jdbcTemplate.update(sqlGenres, film.getId(), genre.getId());
			}
		}
	}

	private Mpa getMpaFromDataBase(Long id) {
		Mpa mpa = null;
		String sqlMpa = "SELECT mpa_id, mpa_name FROM mpa WHERE mpa_id IN (SELECT mpa_id FROM films_mpa " + "WHERE film_id = ?)";
		SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(sqlMpa, id);
		while (mpaRows.next()) {
			mpa = Mpa.builder().id(mpaRows.getLong("mpa_id")).name(mpaRows.getString("mpa_name")).build();
		}
		return mpa;
	}

	private List<Genre> getGenresFromDataBase(Long id) {
		String sql = "SELECT genre_id, genre_name FROM genres WHERE genre_id IN (SELECT genre_id " + "FROM films_genres WHERE film_id = ?)";
		List<Genre> list = jdbcTemplate.query(sql, (rs, rowNum) -> Genre.builder().id(rs.getLong("genre_id"))
																		.name(rs.getString("genre_name")).build(), id);
		return list;
	}

	@Override
	public void addLike(Long id, Long userId) {
		String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
		jdbcTemplate.update(sql, id, userId);
	}

	@Override
	public void deleteLike(Long id, Long userId) {
		String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
		int rowsUpdated = jdbcTemplate.update(sql, id, userId);
		if (rowsUpdated == 0) {
			throw new NotFoundException("Invalid User ID:  " + userId);
		}
	}

	@Override
	public Collection<Film> getTopRatingFilms(int count) {
		String sql = "SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION FROM FILMS AS F " +
				"LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID GROUP BY F.FILM_ID ORDER BY COUNT(L.USER_ID) DESC "
				+ "LIMIT ?;";

		List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), count);

		for (Film film : films) {
			film.setMpa(getMpaFromDataBase(film.getId()));
			film.setGenres(getGenresFromDataBase(film.getId()));
			film.setDirectors(getDirectorsFromDataBase(film.getId()));
		}

		return films;
	}

	/** ALG_8*/
	@Override
	public Collection<Film> getTopRatingFilmsByGenreAndYear(int count, long genreId, int year) {
		log.debug("ALG_8. вошли в базу");

		List<Film> films;
		StringJoiner joiner = new StringJoiner(" ");
		String sqlEnd = "GROUP BY F.FILM_ID ORDER BY COUNT(L.USER_ID) DESC " +
				"LIMIT ?;";
		joiner.add("SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION FROM FILMS AS F " +
				"LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID " +
				"LEFT JOIN FILMS_GENRES AS FG ON F.FILM_ID = FG.FILM_ID");
		if (genreId != -1 && year != -1) {
			joiner.add("WHERE YEAR(F.RELEASE_DATE) = ? AND FG.GENRE_ID = ?");
			String sql = joiner.add(sqlEnd)
					.toString();
			log.info("вошли в поиск 2/2000");
			films = jdbcTemplate.query(sql, new FilmRowMapper(), year, genreId, count);
		} else if (genreId != -1) {
			joiner.add("WHERE FG.GENRE_ID = ?");
			String sql = joiner.add(sqlEnd)
					.toString();
			films = jdbcTemplate.query(sql, new FilmRowMapper(), genreId, count);
		} else {
			joiner.add("WHERE YEAR(F.RELEASE_DATE) = ?");
			String sql = joiner.add(sqlEnd)
					.toString();
			films = jdbcTemplate.query(sql, new FilmRowMapper(), year, count);
		}

		for (Film film : films) {
			film.setMpa(getMpaFromDataBase(film.getId()));
			film.setGenres(getGenresFromDataBase(film.getId()));
			film.setDirectors(getDirectorsFromDataBase(film.getId()));
		}
		return films;
	}

	@Override
	public Collection<Genre> getAllGenres() {
		String sql = "SELECT genre_id, genre_name FROM genres ORDER BY genre_id";

		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			return Genre.builder().id(rs.getLong("genre_id")).name(rs.getString("genre_name")).build();
		});
	}

	@Override
	public Genre getGenresById(Long id) {
		Genre genre;
		SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE genre_id = ?", id);
		if (genreRows.next()) {
			genre = Genre.builder().id(genreRows.getLong("genre_id")).name(genreRows.getString("genre_name")).build();
			log.info("Genre found: {} {}", id, genreRows.getString("genre_name"));
		} else {
			log.info("Invalid Genre ID: {}", id);
			throw new NotFoundException("Invalid Genre ID:  " + id);
		}
		return genre;
	}

	@Override
	public Collection<Mpa> getAllRatingsMpa() {
		String sql = "SELECT mpa_id, mpa_name FROM mpa ORDER BY mpa_id";

		return jdbcTemplate.query(sql, (rs, rowNum) -> Mpa.builder().id(rs.getLong("mpa_id"))
														  .name(rs.getString("mpa_name")).build());
	}

	@Override
	public Mpa getRatingsMpaById(Long id) {
		Mpa mpa;
		SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM mpa WHERE mpa_id = ?", id);
		if (mpaRows.next()) {
			mpa = Mpa.builder().id(mpaRows.getLong("mpa_id")).name(mpaRows.getString("mpa_name")).build();
			log.info("Mpa found: {} {}", id, mpaRows.getString("mpa_name"));
		} else {
			log.info("Invalid Genre ID: {}", id);
			throw new NotFoundException("Invalid Mpa ID:  " + id);
		}
		return mpa;
	}

	/** ALG_6 */
	@Override
	public void deleteFilm(Long filmId) {
        String sqlQuery = "DELETE FROM films WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId);
        log.info("ALG_6. Film ID " + filmId + " deleted");
	}

	private void setDirectorInDataBase(Film film) {
		String sqlDeleteDirector = "DELETE FROM films_directors WHERE film_id = ?";
		jdbcTemplate.update(sqlDeleteDirector, film.getId());

		if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
			List<Director> list = film.getDirectors().stream().distinct().collect(Collectors.toList());
			film.setDirectors(list);
			for (Director director : list) {
				String sqlDirectors = "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)";
				jdbcTemplate.update(sqlDirectors, film.getId(), director.getId());
			}
		}
	}

	private List<Director> getDirectorsFromDataBase(Long id) {
		String sql = "SELECT * FROM directors WHERE director_id IN (SELECT director_id FROM films_directors WHERE " + "film_id = ?)";
		return jdbcTemplate.query(sql, (rs, rowNum) -> Director.builder().id(rs.getLong("director_id"))
															   .name(rs.getString("director_name")).build(), id);
	}

	/**
	 * ALG_7
	 */
	public Collection<Film> getAllFilmsByDirector(Long id, String sortBy) {
		SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM directors WHERE director_id = ?", id);
		if (mpaRows.next()) {
			log.info("ALG_7. Director found: {}", id);
		} else {
			log.info("ALG_7. Invalid Director ID: {}", id);
			throw new NotFoundException("ALG_7. Invalid Director ID: " + id);
		}

		String sql;
		if (Objects.equals(sortBy, "likes")) {
			sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, COUNT(l.user_id) " +
					"FROM films f LEFT JOIN likes l ON f.film_id = l.film_id INNER JOIN films_directors fd ON f.film_id = fd.film_id " +
					"WHERE fd.director_id = ? " +
					"GROUP BY f.film_id, f.name, f.description, " + "f.release_date, f.duration " +
					"ORDER BY COUNT(l.user_id) DESC";
		} else if (Objects.equals(sortBy, "year")) {
			sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration " +
					"FROM films f JOIN films_directors fd ON f.film_id = fd.film_id " +
					"WHERE fd.director_id = ? " +
					"ORDER BY f.release_date";
		} else {
			throw new NotFoundException("ALG_7. Invalid RequestParam:  " + sortBy);
		}

		List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), id);

		for (Film film : films) {
			film.setMpa(getMpaFromDataBase(film.getId()));
			film.setGenres(getGenresFromDataBase(film.getId()));
			film.setDirectors(getDirectorsFromDataBase(film.getId()));
		}
		return films;
	}

	/**
	 * ALG_2
	 */
	@Override
	public Collection<Film> searchFilms(String query, String by) {
		String sql;
		if (Objects.equals(by, "director")) {
			sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, COUNT(l.film_id) as likes_count " +
					"FROM films f JOIN films_directors fd ON f.film_id = fd.film_id JOIN directors d ON fd.director_id = d.director_id " +
					"LEFT JOIN likes l ON f.film_id = l.film_id " +
					"WHERE LOWER(d.director_name) LIKE LOWER('%" + query + "%') " +
					"GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration " +
					"ORDER BY likes_count";
		} else if (Objects.equals(by, "title")) {
			sql = "SELECT films.film_id, films.name, films.description, films.release_date, films.duration, " +
					"COUNT (likes.film_id) as likes_count " +
					"FROM films LEFT JOIN likes ON films.film_id = likes.film_id " +
					"WHERE LOWER(films.name) LIKE LOWER('%" + query + "%') " +
					"GROUP BY films.film_id, films" + ".name, films.description, films.release_date, films.duration " +
					"ORDER BY likes_count";
		} else if (Objects.equals(by, "title,director") || Objects.equals(by, "director,title")) {
			sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, COUNT(l.film_id) AS likes_count " +
					"FROM films f LEFT JOIN films_directors fd ON f.film_id = fd.film_id " +
					"LEFT JOIN directors d ON " + "fd.director_id = d.director_id LEFT JOIN likes l ON f.film_id = l.film_id " +
					"WHERE LOWER (d.director_name) " + "LIKE LOWER ('%" + query + "%') OR LOWER (f.name) LIKE LOWER ('%" + query + "%') " +
					"GROUP BY f.film_id, f.name, " + "f.description, f.release_date, f.duration " +
					"ORDER BY likes_count DESC";
		} else {
			throw new NotFoundException("ALG_2. Invalid RequestParam:  " + by);
		}

		List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());

		for (Film film : films) {
			film.setMpa(getMpaFromDataBase(film.getId()));
			film.setGenres(getGenresFromDataBase(film.getId()));
			film.setDirectors(getDirectorsFromDataBase(film.getId()));
		}
		log.info("ALG_2. searchFilms in work");
		return films;
	}

	private void checkContainsUserInDatabase(Long id) {
		SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE user_id = ?", id);
		if (sqlRows.first()) {
			log.info("ALG_3. User found: {}", id);
		} else {
			log.info("ALG_3. User not found: {}", id);
			throw new NotFoundException("ALG_3. User not found: " + id);
		}
	}

	private void checkContainsFriendsOfUser(Long userId, Long friendId) {
		SqlRowSet sqlRows = jdbcTemplate.queryForRowSet("SELECT * FROM friends WHERE user_id = ? AND friend_id = ?", userId, friendId);
		if (sqlRows.first()) {
			log.info("ALG_3. Friend {} of User {} found", friendId, userId);
		} else {
			log.info("ALG_3. Friend {} of User {} not found", friendId, userId);
			throw new NotFoundException("ALG_3. Friend " + userId + " of User " + friendId + " not found");
		}
	}

	/**
	 * ALG_3
	 */
	@Override
	public Collection<Film> getCommonFilms(Long userId, Long friendId) {
		if (Objects.equals(userId, friendId)) {
			throw new IllegalArgumentException("ALG_3. UserId and friendId must not be the same: " + userId + "=" + friendId);
		}
		checkContainsUserInDatabase(userId);
		//checkContainsUserInDatabase(friendId);
		checkContainsFriendsOfUser(userId, friendId);

		String sql = "";


		List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());

		for (Film film : films) {
			film.setMpa(getMpaFromDataBase(film.getId()));
			film.setGenres(getGenresFromDataBase(film.getId()));
			film.setDirectors(getDirectorsFromDataBase(film.getId()));
		}
		return films;
	}

}
