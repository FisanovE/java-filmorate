package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchSetup;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ValidateService;

import java.util.Collection;
import java.util.Objects;

@Slf4j
@RestController
@Component
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final ValidateService validateService;

    @PostMapping
    public Film addNewFilm(@RequestBody Film film) {
        validateService.checkingFilmForValid(film);
        return filmService.addNewFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        validateService.checkIdNotNull(film.getId());
        validateService.checkingFilmForValid(film);
        return filmService.updateFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable(required = false) Long id) {
        return filmService.getFilmById(id);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable(required = false) Long id, @PathVariable(required = false) Long userId) {
        filmService.deleteLike(id, userId);
    }

    /**
     * ALG_8
     */
    @GetMapping("/popular")
    public Collection<Film> getTopRatingFilms(@RequestParam(defaultValue = "10", required = false) Integer count, @RequestParam(defaultValue = "-1", required = false) Long genreId, @RequestParam(defaultValue = "-1", required = false) Integer year) {
        if (genreId != -1 || year != -1) {
            return filmService.getTopRatingFilmsByGenreAndYear(count, genreId, year);
        } else {
            return filmService.getTopRatingFilms(count);
        }
    }

    /**
     * ALG_7
     */
    @GetMapping("/director/{directorId}")
    public Collection<Film> getAllFilmsByDirector(@PathVariable Long directorId, @RequestParam String sortBy) {
        if (Objects.equals(sortBy, "year") || Objects.equals(sortBy, "likes")) {
            return filmService.getAllFilmsByDirector(directorId, sortBy);
        } else {
            throw new NotFoundException("ALG_7. Invalid RequestParam:  " + sortBy);
        }
    }

    /**
     * ALG_2
     */
    @GetMapping("/search")
    public Collection<Film> searchFilms(@RequestParam String query, @RequestParam String by) {
        String[] fields = by.split(",");
        if (fields.length == 1) {
            SearchSetup setup = SearchSetup.valueOf(fields[0]);
            switch (setup) {
                case director:
                case title:
                    return filmService.searchFilms(query, by);
                default:
                    throw new NotFoundException("ALG_2. Invalid search param:  " + by);
            }
        } else if (fields.length == 2) {
            SearchSetup setup1 = SearchSetup.valueOf(fields[0]);
            SearchSetup setup2 = SearchSetup.valueOf(fields[1]);
            if ((setup1 == SearchSetup.director && setup2 == SearchSetup.title) ||
                    (setup1 == SearchSetup.title && setup2 == SearchSetup.director)) {
                return filmService.searchFilms(query, by);
            } else {
                throw new NotFoundException("ALG_2. Invalid search param:  " + by);
            }
        }
        throw new NotFoundException("ALG_2. Invalid search param:  " + by);
    }

    /**
     * ALG_6
     */
    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable Long id) {
        filmService.deleteFilm(id);
    }

    /**
     * ALG_3
     */
    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }
}
