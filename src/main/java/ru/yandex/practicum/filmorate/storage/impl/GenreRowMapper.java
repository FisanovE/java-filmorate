package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GenreRowMapper implements RowMapper<Genre> {
    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getLong("genre_id"))
                .name(rs.getString("genre_name"))
                .build();
    }
}