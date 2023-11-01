package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import java.util.List;

public interface EventStorage {
    /**
     * ALG5
     */
    List<Event> get(Long userId);

    void create(Long userId, EventType eventType, OperationType operation, Long entityId);
}
