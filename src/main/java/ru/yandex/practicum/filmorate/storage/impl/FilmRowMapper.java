package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Objects;

public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = null;
        if (rs.getLong("mpa.mpa_id") != 0) {
            mpa = Mpa.builder()
                    .name(rs.getString("mpa.mpa_name"))
                    .id(rs.getLong("mpa.mpa_id"))
                    .build();
        }

        return Film.builder().id(rs.getLong("film_id")).name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(Objects.requireNonNull(rs.getDate("release_date")).toLocalDate())
                .duration(rs.getInt("duration"))
                .genres(new LinkedHashSet<>())
                .mpa(mpa)
                .directors(new LinkedHashSet<>())
                .build();
    }
}
