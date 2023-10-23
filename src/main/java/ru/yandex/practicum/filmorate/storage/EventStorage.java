package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {
    /**
     * ALG5
     */
    List<Event> get(Long userId);

    void create(Long userId, String eventType, String operation, Long entityId);
}
