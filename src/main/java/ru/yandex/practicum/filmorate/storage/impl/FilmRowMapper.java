package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class FilmRowMapper implements RowMapper<Film> {
	@Override
	public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
		return Film.builder()
				   .id(rs.getLong("film_id"))
				   .name(rs.getString("name"))
				   .description(rs.getString("description"))
				   .releaseDate(Objects.requireNonNull(rs.getDate("release_date")).toLocalDate())
				   .duration(rs.getInt("duration"))
				   .build();
	}
}