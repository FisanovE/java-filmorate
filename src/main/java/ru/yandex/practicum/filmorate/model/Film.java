package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;

@Getter
@Setter
@Builder
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private LinkedHashSet<Genre> genres;
    private Mpa mpa;
    /**
     * ALG_7
     */
    private LinkedHashSet<Director> directors;
}
