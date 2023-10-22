package ru.yandex.practicum.filmorate.exeptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(final NotFoundException e) {
        log.info(("Exception! In DB not found entity"));
        e.printStackTrace();
        return new ErrorResponse("Invalid ID.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(final ValidationException e) {
        log.info(("Exception! Entity is not validated"));
        e.printStackTrace();
        return new ErrorResponse("Validation error.", e.getMessage());
    }
}
