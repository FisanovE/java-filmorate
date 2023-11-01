package ru.yandex.practicum.filmorate.utils;

import org.springframework.core.convert.converter.Converter;
import ru.yandex.practicum.filmorate.model.enums.SearchParameter;

public class StringToSearchParameterConverter implements Converter<String, SearchParameter> {
    @Override
    public SearchParameter convert(String source) {
        try {
            return SearchParameter.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
