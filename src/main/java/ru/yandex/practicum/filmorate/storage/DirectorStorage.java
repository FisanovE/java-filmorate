package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.SQLException;
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

    void save(Collection<Film> film) throws SQLException;

    void load(Collection<Film> films);
}

