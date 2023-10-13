package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;

/**
 * ALG_7
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

	private final JdbcTemplate jdbcTemplate;
	private static final String sqlAddNewDirector = "INSERT INTO directors (director_name) VALUES (?)";
	private static final String sqlUpdateDirector = "UPDATE directors SET director_name = ? WHERE director_id = ?";
	private static final String sqlGetDirectorById = "SELECT * FROM directors WHERE director_id = ?";
	private static final String sqlGetAllDirectors = "SELECT * FROM directors ORDER BY director_id";
	private static final String sqlDeleteDirectorById = "DELETE FROM directors WHERE director_id = ?";


	@Override
	public Director addNewDirector(Director director) {
		if (director.getName().isBlank()) {
			throw new ValidationException("ALG_7. Invalid name format: \"" + director.getName() + "\"");
		}
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sqlAddNewDirector, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, director.getName());
			return ps;
		}, keyHolder);

		Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
		director.setId(generatedId);

		log.info("ALG_7. Director added: {} {}", director.getId(), director.getName());
		return director;
	}

	@Override
	public Director updateDirector(Director director) {
		int rowsUpdated = jdbcTemplate.update(sqlUpdateDirector, director.getName(), director.getId());
		log.info("ALG_7. Director update: {} {}", director.getId(), director.getName());
		if (rowsUpdated != 1) {
			throw new NotFoundException("ALG_7. Invalid Director ID:  " + director.getId());
		}
		return director;
	}

	@Override
	public Collection<Director> getAllDirectors() {
		log.info("ALG_7. getAllDirectors in work");
		return jdbcTemplate.query(sqlGetAllDirectors, (rs, rowNum) -> Director.builder().id(rs.getLong("director_id"))
																			  .name(rs.getString("director_name"))
																			  .build());
	}

	@Override
	public Director getDirectorById(Long id) {
		Director director;
		SqlRowSet directorRows = jdbcTemplate.queryForRowSet(sqlGetDirectorById, id);
		if (directorRows.first()) {
			director = Director.builder().id(directorRows.getLong("director_id"))
							   .name(directorRows.getString("director_name")).build();
			log.info("ALG_7. Director found: {} {}", id, directorRows.getString("director_name"));
		} else {
			log.info("ALG_7. Invalid Director ID: {}", id);
			throw new NotFoundException("ALG_7. Invalid Director ID:  " + id);
		}
		return director;
	}

	@Override
	public void deleteDirectorById(Long id) {
		int rowsUpdated = jdbcTemplate.update(sqlDeleteDirectorById, id);
		log.info("ALG_7. Director deleted: {}", id);
		if (rowsUpdated == 0) {
			throw new NotFoundException("ALG_7. Invalid Director ID:  " + id);
		}
	}
}
