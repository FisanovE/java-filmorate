package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Data
@Builder
public class Genre {
    private Long id;
    private String name;
}