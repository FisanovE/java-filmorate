package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
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

	@Autowired
	public FilmDbStorage(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Film addNewFilm(Film film) {
		String sqlRequest = "INSERT INTO films (name, description, release_date, duration) VALUES (?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();

		int rowsUpdated = jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sqlRequest, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, film.getName());
			ps.setString(2, film.getDescription());
			ps.setDate(3, Date.valueOf(film.getReleaseDate()));
			ps.setInt(4, film.getDuration());
			return ps;
		}, keyHolder);

		if (rowsUpdated == 0) {
			throw new NotFoundException("Invalid Film ID:  " + film.getId());
		}

		Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
		film.setId(generatedId);

		setMpaInDataBase(film);
		setGenreInDataBase(film);

		log.info("Film added: {} {}", film.getId(), film.getName());
		return film;
	}

	@Override
	public Film updateFilm(Film film) {
		String sqlRequest = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ? " + "WHERE film_id = ?";
		int rowsUpdated = jdbcTemplate.update(sqlRequest, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getId());
		if (rowsUpdated == 0) {
			throw new NotFoundException("Invalid Film ID:  " + film.getId());
		}
		setMpaInDataBase(film);
		setGenreInDataBase(film);
		log.info("Film update: {} {}", film.getId(), film.getName());
		return film;
	}

	@Override
	public Film getFilmById(Long id) {
		try {
			String sql = "SELECT * FROM films WHERE film_id = ?";
			Film film = jdbcTemplate.queryForObject(sql, new FilmRowMapper(), id);
			film.setMpa(getMpaFromDataBase(id));
			film.setGenres(getGenresFromDataBase(id));
			log.info("Film found: {} {}", id, film.getName());
			return film;
		} catch (RuntimeException ex) {
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
		return jdbcTemplate.query(sql, (rs, rowNum) -> Genre.builder().id(rs.getLong("genre_id"))
															.name(rs.getString("genre_name")).build(), id);
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
		String sql = "SELECT F.FILM_ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION FROM FILMS AS F " + "LEFT JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID GROUP BY F.FILM_ID ORDER BY COUNT(L.USER_ID) DESC " + "LIMIT ?;";

		List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), count);

		for (Film film : films) {
			film.setMpa(getMpaFromDataBase(film.getId()));
			film.setGenres(getGenresFromDataBase(film.getId()));
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

	@Override
	public void deleteFilm(Long filmId) {
		final String sqlQuery = "DELETE FROM films WHERE FILM_ID = ?";
		jdbcTemplate.update(sqlQuery, filmId);
	}
}
