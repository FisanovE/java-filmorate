package ru.yandex.practicum.filmorate.utils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateIsAfterThisValidator implements ConstraintValidator<DateIsAfter, LocalDate> {
	private LocalDate minimumDate;

	public DateIsAfterThisValidator() {
	}

	@Override
	public void initialize(DateIsAfter constraintAnnotation) {
		minimumDate = LocalDate.parse(constraintAnnotation.value());
	}

	@Override
	public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
		return value == null || !value.isBefore(minimumDate);
	}
}
