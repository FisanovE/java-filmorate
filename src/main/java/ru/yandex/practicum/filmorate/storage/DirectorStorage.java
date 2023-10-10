package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {

	Director addNewDirector(Director director);

	Director updateDirector(Director director);

	Collection<Director> getAllDirectors();

	Director getDirectorById(Long id);

	void deleteDirectorById(Long id);
}

