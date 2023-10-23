package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcOperations jdbcOperations;

    @Override
    public Collection<Mpa> getAll() {
            String sql = "SELECT mpa_id, mpa_name FROM mpa ORDER BY mpa_id";
            return jdbcOperations.query(sql, new MpaRowMapper());
    }

    @Override
    public Mpa getById(Long id) {
        //checkContainsMpa(id);
        try {
        String sql = "SELECT * FROM mpa WHERE mpa_id = ?";
        return jdbcOperations.queryForObject(sql, new MpaRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Invalid Director ID: " + id);
        }
    }
}
