package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private final JdbcOperations jdbcOperations;
    private final UserDbStorage userDbStorage;

    /**
     * ALG5
     */
    @Override
    public void create(Long userId, String eventType, String operation, Long entityId) {
        String sql = "INSERT INTO events (user_id, event_type, operation, entity_id, time_stamp) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcOperations.update(sql, userId, eventType, operation, entityId, System.currentTimeMillis());
    }

    /**
     * ALG5
     */
    @Override
    public List<Event> get(Long userId) {
        userDbStorage.checkContainsUser(userId);
        String sql = "SELECT * FROM events WHERE user_id = ?";
        return jdbcOperations.query(sql, new EventRowMapper(), userId);
    }
}
