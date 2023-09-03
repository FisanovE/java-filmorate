package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.utils.DateIsAfter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class Film {
	private Long id;

	@NotBlank (message = "Film title must not be empty")
	private String name;

	@NotNull
	@Size (max = 200, message = "The maximum description length is 200 characters")
	private String description;

	@DateIsAfter
	private LocalDate releaseDate;

	@Positive (message = "The duration of the film should be positive")
	private int duration;

	private Set<Long> likes;
}
