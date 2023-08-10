package ru.yandex.practicum.filmorate.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
	int id;

	@NotBlank (message = "Поле Email не должно быть пустым")
	@Email (message = "Не верный формат email")
	private String email;

	@NotBlank (message = "Поле Login не должно быть пустым")
	private String login;

	private String name;

	@Past (message = "Дата рождения не должна быть в будущем")
	//@NotNull
	private LocalDate birthday;

	/*public User(String email, String login, LocalDate birthday) {
		this.email = email;
		this.login = login;
		this.birthday = birthday;
	}

	public User(String email, String login, String name, LocalDate birthday) {
		this.email = email;
		this.login = login;
		this.name = name;
		this.birthday = birthday;
	}*/
}
