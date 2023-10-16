package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

/**
 * ALG_5
 */
@Data
@Builder
public class Event {
    private Long eventId;
    private Long userId;
    private String eventType;
    private String operation;
    private Long entityId;
    private Long timestamp;
}
