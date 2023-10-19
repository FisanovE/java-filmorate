package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final FilmStorage filmStorage;

    public Collection<Genre> getAllGenres() {
        return filmStorage.getAllGenres();
    }

    public Genre getGenresById(Long id) {
        return filmStorage.getGenresById(id);
    }

}
