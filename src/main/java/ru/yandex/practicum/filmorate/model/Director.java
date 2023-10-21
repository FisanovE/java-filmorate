package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * ALG_7
 */
@Data
@Builder
@ToString
public class Director {
    private Long id;
    private String name;
}