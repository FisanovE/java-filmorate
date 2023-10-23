package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface MpaStorage {

    Collection<Mpa> getAllRatingsMpa();

    Mpa getRatingsMpaById(Long id);

    SqlRowSet getMpaRow(Long id);
}
