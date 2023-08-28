package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.yandex.practicum.filmorate.utils.CustomLocalDateAdapter;

import java.time.LocalDate;

@SpringBootApplication
public class FilmorateApplication {

	public static void main(String[] args) {
		SpringApplication.run(FilmorateApplication.class, args);
	}

	@Bean
	public Gson getGson() {
		return new GsonBuilder().registerTypeAdapter(LocalDate.class, new CustomLocalDateAdapter().nullSafe()).create();
	}
}
