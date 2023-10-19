package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

/**
 * ALG_7
 */
@Slf4j
@RestController
@Component
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @PostMapping
    public Director addNewDirector(@RequestBody Director director) {
        log.info("Create director");
        return directorService.addNewDirector(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody Director director) {
        log.info("Update director {}", director.getId());
        return directorService.updateDirector(director);
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Long id) {
        log.info("Get directors {}", id);
        return directorService.getDirectorById(id);
    }

    @GetMapping
    public Collection<Director> getAllDirectors() {
        log.info("Get directors");
        return directorService.getAllDirectors();
    }

    @DeleteMapping("/{id}")
    public void deleteDirectorById(@PathVariable Long id) {
        log.info("Delete directors {}", id);
        directorService.deleteDirectorById(id);
    }

}
