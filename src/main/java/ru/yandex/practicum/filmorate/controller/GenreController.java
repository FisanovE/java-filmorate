package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;


@Slf4j
@RestController
@Component
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {

    private final GenreService filmService;

    @GetMapping
    public Collection<Genre> getAllGenres() {
        log.info("Get genres");
        return filmService.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenresById(@PathVariable(required = false) Long id) {
        log.info("Get genre {}", id);
        return filmService.getGenresById(id);
    }

}
