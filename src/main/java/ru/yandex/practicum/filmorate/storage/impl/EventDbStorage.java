package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcOperations jdbcOperations;

    /**
     * ALG5
     */
    @Override
    public void addEvent(Long userId, String eventType, String operation, Long entityId) {
        String sql = "INSERT INTO events (user_id, event_type, operation, entity_id, time_stamp) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcOperations.update(sql, userId, eventType, operation, entityId, System.currentTimeMillis());
    }

    /**
     * ALG5
     */
    @Override
    public List<Event> getEvents(Long userId) {
        checkContainsUserInDatabase(userId);
        String sql = "SELECT * FROM events WHERE user_id = ?";
        return jdbcOperations.query(sql, new EventRowMapper(), userId);
    }

    private void checkContainsUserInDatabase(Long id) {
        SqlRowSet sqlRows = jdbcOperations.queryForRowSet("SELECT * FROM users WHERE user_id = ?", id);
        if (sqlRows.first()) {
            log.info("User found: {}", id);
        } else {
            log.info("User not found: {}", id);
            throw new NotFoundException("User not found: " + id);
        }
    }
}
