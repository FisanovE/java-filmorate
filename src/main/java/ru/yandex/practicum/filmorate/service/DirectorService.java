package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
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

    public Director addNewDirector(Director director) {
        if (director.getName().isBlank()) {
            throw new ValidationException("ALG_7. Invalid name format: \"" + director.getName() + "\"");
        }
        return directorStorage.addNewDirector(director);
    }

    public Director updateDirector(Director director) {
        validateService.checkContainsDirectorInDatabase(director.getId());
        return directorStorage.updateDirector(director);
    }

    public Director getDirectorById(Long directorId) {
        return directorStorage.getDirectorById(directorId);
    }

    public Collection<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public void deleteDirectorById(Long directorId) {
        validateService.checkContainsDirectorInDatabase(directorId);
        directorStorage.deleteDirectorById(directorId);
    }

}
