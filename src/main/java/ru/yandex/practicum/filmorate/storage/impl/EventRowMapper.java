package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ALG_5
 */
public class EventRowMapper implements RowMapper<Event> {

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {


        return Event.builder()
                .eventId(rs.getLong("event_id"))
                .userId(rs.getLong("user_id"))
                .eventType(EventType.valueOf(rs.getString("event_type").toUpperCase()))
                .operation(OperationType.valueOf(rs.getString("operation").toUpperCase()))
                .entityId(rs.getLong("entity_id"))
                .timestamp(rs.getLong("time_stamp"))
                .build();
    }
}
