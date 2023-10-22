package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.EventService;

import java.util.Collection;

@Slf4j
@RestController
@Component
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    /**
     * ALG_5
     */
    @GetMapping("/users/{id}/feed")
    public Collection<Event> getEvents(@PathVariable("id") Long userId) {
        log.info("Get events of user {}", userId);
        return eventService.getEvents(userId);
    }
}
