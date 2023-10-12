package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;

/** ALG_7 */
public class DirectorRowMapper implements RowMapper<Director> {
	@Override
	public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
		return Director.builder()
					   .id(rs.getLong("director_id"))
					   .name(rs.getString("director_name"))
					   .build();
	}
}