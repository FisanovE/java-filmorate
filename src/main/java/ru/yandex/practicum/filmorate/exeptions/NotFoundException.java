package ru.yandex.practicum.filmorate.exeptions;

public class NotFoundException extends RuntimeException {
	public NotFoundException(final String message) {
		super(message);
	}
}
