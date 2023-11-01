package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.SearchParameter;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidateService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Component
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final ValidateService validateService;

    @PostMapping
    public Film create(@RequestBody Film film) {
        validateService.checkingFilmForValid(film);
        log.info("Create film");
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        validateService.checkIdNotNull(film.getId());
        log.info("Update film {}", film.getId());
        return filmService.update(film);
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable(required = false) Long id) {
        log.info("Get film {}", id);
        return filmService.getById(id);
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Get films");
        return filmService.getAll();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Update film {}, liked user {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable(required = false) Long id, @PathVariable(required = false) Long userId) {
        log.info("Delete in film {}, like user {}", id, userId);
        filmService.deleteLike(id, userId);
    }

    /**
     * ALG_8
     */
    @GetMapping("/popular")
    public Collection<Film> getTopRatingFilms(@RequestParam(defaultValue = "10", required = false) Integer count,
                                              @RequestParam(defaultValue = "-1", required = false) Long genreId,
                                              @RequestParam(defaultValue = "-1", required = false) Integer year) {
        log.info("Get rating films, count {}, genreId {}, year {}", count, genreId, year);
        return filmService.getTopRatingFilmsByGenreAndYear(count, genreId, year);
    }

    /**
     * ALG_7
     */
    @GetMapping("/director/{directorId}")
    public Collection<Film> getAllFilmsByDirector(@PathVariable Long directorId, @RequestParam String sortBy) {
        log.info("Get films/directorId {} sortBy {} ", directorId, sortBy);
        return filmService.getAllFilmsByDirector(directorId, sortBy);
    }

    /**
     * ALG_2
     */
    @GetMapping("/search")
    public Collection<Film> searchFilms(@RequestParam String query, @RequestParam String by) {
        log.info("Get films/search {} by {} ", query, by);
        return filmService.searchFilms(query, new ArrayList<>(List.of(by.split(","))).stream()
                .map(String::toUpperCase)
                .map(SearchParameter::valueOf)
                .collect(Collectors.toList()));
    }

    /**
     * ALG_6
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        filmService.delete(id);
        log.info("Delete film {}", id);
    }

    /**
     * ALG_3
     */
    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        log.info("Get films/common userId {} friendId {} ", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }
}
