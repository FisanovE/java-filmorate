package ru.yandex.practicum.filmorate.exeptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(final NotFoundException e) {
        return new ErrorResponse("Invalid ID.", e.getMessage(), Arrays.toString(e.getStackTrace()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final ValidationException e) {
        return new ErrorResponse("Validation error.", e.getMessage(), Arrays.toString(e.getStackTrace()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handle(final NullPointerException e) {
        return new ErrorResponse("Internal error.", e.getMessage(), Arrays.toString(e.getStackTrace()));
    }
}
