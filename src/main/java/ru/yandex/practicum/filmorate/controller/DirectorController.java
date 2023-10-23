package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.ValidateService;

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
    private final ValidateService validateService;

    @PostMapping
    public Director create(@RequestBody Director director) {
        validateService.checkNameNotBlank(director.getName());
        log.info("Create director");
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        validateService.checkIdNotNull(director.getId());
        validateService.checkNameNotBlank(director.getName());
        log.info("Update director {}", director.getId());
        return directorService.update(director);
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable Long id) {
        log.info("Get directors {}", id);
        return directorService.getById(id);
    }

    @GetMapping
    public Collection<Director> getAll() {
        log.info("Get directors");
        return directorService.getAll();
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        log.info("Delete directors {}", id);
        directorService.delete(id);
    }

}
