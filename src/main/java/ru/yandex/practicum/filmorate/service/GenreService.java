package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenresStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenresStorage genresStorage;
    private final ValidateService validateService;

    public Collection<Genre> getAll() {
        return genresStorage.getAllGenres();
    }

    public Genre getById(Long id) {
        validateService.checkGenreValidity(id);
        return genresStorage.getGenresById(id);
    }

}
