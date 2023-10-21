package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface MpaStorage {

    Collection<Mpa> getAllRatingsMpa();

    Mpa getRatingsMpaById(Long id);
}
