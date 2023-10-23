package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenresStorage {

    Collection<Genre> getAllGenres();

    Genre getGenreById(Long id);

    void save(Collection<Film> film);

    void load(Collection<Film> films);
}
