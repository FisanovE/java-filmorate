package ru.yandex.practicum.filmorate.exeptions;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors (chain = true)
public class ErrorResponse {
	private String error;
	private String description;

	public ErrorResponse(String error, String description) {
		this.error = error;
		this.description = description;
	}
}
