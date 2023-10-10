package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Repository
public class DirectorDbStorage implements DirectorStorage {

	private final JdbcTemplate jdbcTemplate;
	private DirectorRowMapper directorRowMapper;
	private static final String sqlAddNewDirector = "INSERT INTO directors (director_name) VALUES (?)";
	private static final String sqlUpdateDirector = "UPDATE directors SET director_name = ? WHERE director_id = ?";
	private static final String sqlGetDirectorById = "SELECT * FROM directors WHERE director_id = ?";
	private static final String sqlGetAllDirectors = "SELECT * FROM directors ORDER BY director_id";
	private static final String sqlDeleteDirectorById = "DELETE FROM directors WHERE director_id = ?";


	@Autowired
	public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Director addNewDirector(Director director) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sqlAddNewDirector, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, director.getName());
			return ps;
		}, keyHolder);

		Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
		director.setId(generatedId);

		log.info("Director added: {} {}", director.getId(), director.getName());
		return director;
	}

	@Override
	public Director updateDirector(Director director) {
		try {int rowsUpdated = jdbcTemplate.update(sqlUpdateDirector, director.getName(), director.getId());
			log.info("Director update: {} {}", director.getId(), director.getName());
			return director;
		} catch (DataIntegrityViolationException e) {
			throw new NotFoundException("Invalid Director ID:  " + director.getId());
		}
	}

	@Override
	public Collection<Director> getAllDirectors() {
		return jdbcTemplate.query(sqlGetAllDirectors, (rs, rowNum) -> Director.builder()
																		  .id(rs.getLong("director_id"))
																		  .name(rs.getString("director_name"))
																		  .build());
	}

	@Override
	public Director getDirectorById(Long id) {
		Director director;
		SqlRowSet directorRows = jdbcTemplate.queryForRowSet(sqlGetDirectorById, id);
		if (directorRows.next()) {
			director = Director.builder()
							   .id(directorRows.getLong("director_id"))
							   .name(directorRows.getString("director_name"))
							   .build();
			log.info("Director found: {} {}", id, directorRows.getString("director_name"));
		} else {
			log.info("Invalid Director ID: {}", id);
			throw new NotFoundException("Invalid Director ID:  " + id);
		}
		return director;
	}

	@Override
	public void deleteDirectorById(Long id) {
		int rowsUpdated = jdbcTemplate.update(sqlDeleteDirectorById, id);
		if (rowsUpdated == 0) {
			throw new NotFoundException("Invalid Director ID:  " + id);
		}
	}

}
