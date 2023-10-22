package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {
    /**
     * ALG5
     */
    List<Event> getEvents(Long userId);

    void addEvent(Long userId, String eventType, String operation, Long entityId);
}
