package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;

public interface FilmStorage {

	Film addNewFilm(Film film);

	Film updateFilm(Film film);

	Collection<Film> getAllFilms();
}

