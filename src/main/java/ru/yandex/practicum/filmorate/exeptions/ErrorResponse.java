package ru.yandex.practicum.filmorate.exeptions;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ErrorResponse {
    private String error;
    private String description;
    private String stacktrace;

    public ErrorResponse(String error, String description, String stacktrace) {
        this.error = error;
        this.description = description;
        this.stacktrace = stacktrace;
    }
}
