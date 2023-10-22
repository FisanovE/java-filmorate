package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

/**
 * ALG_7
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final ValidateService validateService;

    private final DirectorStorage directorStorage;

    public Director create(Director director) {
        validateService.checkNameNotBlank(director.getName());
        return directorStorage.addNewDirector(director);
    }

    public Director update(Director director) {
        validateService.checkIdNotNull(director.getId());
        validateService.checkNameNotBlank(director.getName());
        validateService.checkContainsDirectorInDatabase(director.getId());
        directorStorage.updateDirector(director);
        return director;
    }

    public Director getById(Long directorId) {
        validateService.checkContainsDirectorInDatabase(directorId);
        return directorStorage.getDirectorById(directorId);
    }

    public Collection<Director> getAll() {
        return directorStorage.getAllDirectors();
    }

    public void delete(Long directorId) {
        validateService.checkContainsDirectorInDatabase(directorId);
        directorStorage.deleteDirectorById(directorId);
    }

}
