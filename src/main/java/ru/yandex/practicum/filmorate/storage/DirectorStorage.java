package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

/**
 * ALG_7
 */
public interface DirectorStorage {

    Director create(Director director);

    void update(Director director);

    Collection<Director> getAll();

    Director getById(Long id);

    void delete(Long id);

    void save(Collection<Film> film);

    void load(Collection<Film> films);
}

