package ru.yandex.practicum.filmorate.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;

public class GsonUtils {

	public static Gson getGson() {
		return new GsonBuilder().registerTypeAdapter(LocalDate.class, new CustomLocalDateAdapter().nullSafe()).create();
	}
}
