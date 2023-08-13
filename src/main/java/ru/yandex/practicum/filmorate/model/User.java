package ru.yandex.practicum.filmorate.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data

@Builder
public class User {
	int id;

	@NotBlank (message = "Поле Email не должно быть пустым")
	@Email (message = "Не верный формат email")
	private String email;

	@NotBlank (message = "Поле Login не должно быть пустым")
	private String login;

	private String name;

	@Past (message = "Дата рождения не должна быть в будущем")
	private LocalDate birthday;

}
