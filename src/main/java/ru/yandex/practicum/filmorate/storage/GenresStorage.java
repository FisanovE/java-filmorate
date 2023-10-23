package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenresStorage {

    Collection<Genre> getAllGenres();

    Genre getGenresById(Long id);

    void save(Collection<Film> film);

    void load(Collection<Film> films);

    SqlRowSet getGenreRow(Long id);

    Long getExistingGenresCountFromGenresSet(String genresIds);
}
