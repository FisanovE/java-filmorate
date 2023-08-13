package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.utils.DateIsAfter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
public class Film {
	private int id;

	@NotBlank (message = "Название фильма не должно быть пустым")
	private String name;

	@Size (max = 200, message = "Длина сообщения не должна быть больше 200 символов")
	private String description;

	@DateIsAfter
	private LocalDate releaseDate;

	@Positive (message = "Продолжительность фильма не должна быть отрицательной")
	private int duration;
}
