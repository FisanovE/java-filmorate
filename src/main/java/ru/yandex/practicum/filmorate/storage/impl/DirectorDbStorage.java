package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;

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
        return director;
    }

    @Override
    public void updateDirector(Director director) {
        String sqlUpdateDirector = "UPDATE directors SET director_name = ? WHERE director_id = ?";
        int rowsUpdated = jdbcTemplate.update(sqlUpdateDirector, director.getName(), director.getId());
    }

    @Override
    public Collection<Director> getAllDirectors() {
        String sqlGetAllDirectors = "SELECT * FROM directors ORDER BY director_id";
        return jdbcTemplate.query(sqlGetAllDirectors, new DirectorRowMapper());
    }

    @Override
    public Director getDirectorById(Long id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        return jdbcTemplate.queryForObject(sql, new DirectorRowMapper(), id);
    }

    @Override
    public void deleteDirectorById(Long id) {
        String sqlDeleteDirectorById = "DELETE FROM directors WHERE director_id = ?";
        int rowsUpdated = jdbcTemplate.update(sqlDeleteDirectorById, id);
    }

    public void loadDirectors(List<Film> films) {
        final Map<Long, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, identity()));
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sqlQuery = "select * from DIRECTORS d, FILMS_DIRECTORS fd " +
                "where fd.DIRECTOR_ID = d.DIRECTOR_ID AND fd.FILM_ID in (" + inSql + ")";
        jdbcTemplate.query(sqlQuery, (rs) -> {
            final Film film = filmById.get(rs.getLong("FILM_ID"));
            film.getDirectors().add(new DirectorRowMapper().mapRow(rs, 0));
        }, films.stream().map(Film::getId).toArray());
    }
}
