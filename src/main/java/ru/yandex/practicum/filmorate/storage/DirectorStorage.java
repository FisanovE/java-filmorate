package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

/**
 * ALG_7
 */
public interface DirectorStorage {

    Director addNewDirector(Director director);

    void updateDirector(Director director);

    Collection<Director> getAllDirectors();

    Director getDirectorById(Long id);

    void deleteDirectorById(Long id);
}

