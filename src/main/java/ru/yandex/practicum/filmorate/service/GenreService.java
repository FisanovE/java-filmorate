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
    private final ValidateService validateService;

    public Collection<Genre> getAll() {
        return filmStorage.getAllGenres();
    }

    public Genre getById(Long id) {
        validateService.checkContainsGenreInDatabase(id);
        return filmStorage.getGenresById(id);
    }

}
