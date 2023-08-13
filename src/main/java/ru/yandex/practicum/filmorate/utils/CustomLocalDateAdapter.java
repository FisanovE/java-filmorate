package ru.yandex.practicum.filmorate.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;

public class CustomLocalDateAdapter extends TypeAdapter<LocalDate> {

	@Override
	public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
		jsonWriter.value(localDate.format(DateUtils.formatter));
	}

	@Override
	public LocalDate read(final JsonReader jsonReader) throws IOException {
		return LocalDate.parse(jsonReader.nextString(), DateUtils.formatter);
	}
}
