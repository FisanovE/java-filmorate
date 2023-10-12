package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

/** ALG_7 */
@Data
@Builder
public class Director {
	private Long id;
	private String name;
}