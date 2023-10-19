package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
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

    @Override
    public Director addNewDirector(Director director) {
        String sqlAddNewDirector = "INSERT INTO directors (director_name) VALUES (?)";
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
        String sqlUpdateDirector = "UPDATE directors SET director_name = ? WHERE director_id = ?";
        int rowsUpdated = jdbcTemplate.update(sqlUpdateDirector, director.getName(), director.getId());
        log.info("ALG_7. Director update: {} {}", director.getId(), director.getName());

        return director;
    }

    @Override
    public Collection<Director> getAllDirectors() {
        String sqlGetAllDirectors = "SELECT * FROM directors ORDER BY director_id";
        log.info("ALG_7. getAllDirectors in work");
        return jdbcTemplate.query(sqlGetAllDirectors, new DirectorRowMapper());
    }

    @Override
    public Director getDirectorById(Long id) {
        String sqlGetDirectorById = "SELECT * FROM directors WHERE director_id = ?";
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
        String sqlDeleteDirectorById = "DELETE FROM directors WHERE director_id = ?";
        int rowsUpdated = jdbcTemplate.update(sqlDeleteDirectorById, id);
        log.info("ALG_7. Director deleted: {}", id);
    }
}
