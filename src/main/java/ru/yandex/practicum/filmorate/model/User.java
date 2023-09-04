package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class User {
	private Long id;

	@NotBlank (message = "Email field must not be empty")
	@Email (message = "Invalid e-mail format")
	private String email;

	@NotNull
	@Pattern (regexp = "^[a-zA-Z0-9]{3,12}$", message = "Login field must not be empty and contain spaces")
	private String login;

	private String name;

	@NotNull
	@PastOrPresent (message = "Date of birth cannot be in the future")
	private LocalDate birthday;

	private Set<Long> friendIds = new HashSet<>();
}
