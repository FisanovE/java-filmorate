package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenresStorage {

    Collection<Genre> getAll();

    Genre getById(Long id);

    void save(Collection<Film> film);

    void load(Collection<Film> films);
}
