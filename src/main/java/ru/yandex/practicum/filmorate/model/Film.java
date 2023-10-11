package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class Film {
	private Long id;
	private String name;
	private String description;
	private LocalDate releaseDate;
	private int duration;
	private List<Long> likedUsersIds;
	private List<Genre> genres;
	private Mpa mpa;
	private String director;
}
