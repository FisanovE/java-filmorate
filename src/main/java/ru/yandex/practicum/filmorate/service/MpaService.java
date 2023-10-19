package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final FilmStorage filmStorage;

    public Collection<Mpa> getAllRatingsMpa() {
        return filmStorage.getAllRatingsMpa();
    }

    public Mpa getRatingsMpaById(Long id) {
        return filmStorage.getRatingsMpaById(id);
    }
}
