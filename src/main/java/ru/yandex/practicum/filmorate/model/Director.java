package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * ALG_7
 */
@Getter
@Setter
@Builder
public class Director {
    private Long id;
    private String name;
}