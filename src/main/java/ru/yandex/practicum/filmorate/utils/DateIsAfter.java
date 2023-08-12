package ru.yandex.practicum.filmorate.utils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.Past;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;

@Retention (RetentionPolicy.RUNTIME)
@Constraint (validatedBy = DateIsAfterThisValidator.class)
@Past
public @interface DateIsAfter {
	String message() default "Дата релиза должна быть не ранее {value}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String value() default "1895-12-28";
}

class DateIsAfterThisValidator implements ConstraintValidator<DateIsAfter, LocalDate> {
	private LocalDate minimumDate;

	@Override
	public void initialize(DateIsAfter constraintAnnotation) {
		minimumDate = LocalDate.parse(constraintAnnotation.value());
	}

	@Override
	public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
		return value == null || !value.isBefore(minimumDate);
	}
}
