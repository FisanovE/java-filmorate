package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

/**
 * ALG_5
 */
@Getter
@Setter
@Builder
public class Event {
    private Long eventId;
    private Long userId;
    private EventType eventType;
    private OperationType operation;
    private Long entityId;
    private Long timestamp;
}
