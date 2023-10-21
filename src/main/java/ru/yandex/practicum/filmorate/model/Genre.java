package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Data
@Builder
@ToString
public class Genre {
    private Long id;
    private String name;
}