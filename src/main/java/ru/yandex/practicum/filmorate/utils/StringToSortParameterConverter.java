package ru.yandex.practicum.filmorate.utils;

import org.springframework.core.convert.converter.Converter;
import ru.yandex.practicum.filmorate.model.SortParameter;

public class StringToSortParameterConverter implements Converter<String, SortParameter> {
    @Override
    public SortParameter convert(String source) {
        try {
            return SortParameter.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
