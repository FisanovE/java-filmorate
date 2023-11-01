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
    private final DirectorStorage directorStorage;

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        directorStorage.update(director);
        return director;
    }

    public void delete(Long directorId) {
        directorStorage.delete(directorId);
    }

    public Director getById(Long directorId) {
        return directorStorage.getById(directorId);
    }

    public Collection<Director> getAll() {
        return directorStorage.getAll();
    }
}
