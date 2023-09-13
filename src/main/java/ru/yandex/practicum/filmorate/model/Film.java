package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class Film {
	private Long id;
	private String name;
	private String description;
	private LocalDate releaseDate;
	private int duration;
	private Set<Long> likedUsersIds = new HashSet<>();
	private Set<String> genre = new HashSet<>();
	private String ratingМРА;
}
