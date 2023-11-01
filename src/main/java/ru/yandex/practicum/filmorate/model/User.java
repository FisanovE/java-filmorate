package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
