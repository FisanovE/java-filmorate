package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenresStorage;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenresDbStorage implements GenresStorage {
    private final JdbcOperations jdbcOperations;

    @Override
    public Collection<Genre> getAll() {
        String sql = "SELECT genre_id, genre_name FROM genres ORDER BY genre_id";
        return jdbcOperations.query(sql, new GenreRowMapper());
    }

    @Override
    public Genre getById(Long id) {
        try {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        return jdbcOperations.queryForObject(sql, new GenreRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Invalid Genre ID: " + id);
        }
    }

    @Override
    public void save(Collection<Film> films) {
        StringBuilder builder = new StringBuilder("insert into films_genres (film_id, genre_id) values");

        for (Film film : films) {
            if (film.getGenres() != null) {
                for (Genre genre : film.getGenres()) {
                    builder.append(String.format(" (%d, %d),", film.getId(), genre.getId()));
                }
            } else {
                film.setGenres(new LinkedHashSet<>());
            }
        }

        if (builder.substring(builder.length() - 1).equals("s")) {
            return;
        }
        builder.deleteCharAt(builder.length() - 1);

        jdbcOperations.update(builder.toString());
    }

    @Override
    public void load(Collection<Film> films) {
        final Map<Long, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, identity()));
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sqlQuery = "select * from GENRES g, films_genres fg " +
                "where fg.GENRE_ID = g.GENRE_ID AND fg.FILM_ID in (" + inSql + ")";
        jdbcOperations.query(sqlQuery, (rs) -> {
            final Film film = filmById.get(rs.getLong("FILM_ID"));
            if (film.getGenres() != null) {
                film.getGenres().add(new GenreRowMapper().mapRow(rs, 0));
            }
        }, filmById.keySet().toArray());
    }
}
