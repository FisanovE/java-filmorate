package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventStorage eventStorage;

    private final ValidateService validateService;

    public List<Event> getEvents(Long userId) {
        validateService.checkContainsUserInDatabase(userId);
        return eventStorage.getEvents(userId);
    }
}