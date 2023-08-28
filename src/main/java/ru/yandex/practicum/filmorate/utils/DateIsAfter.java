package ru.yandex.practicum.filmorate.utils;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Past;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention (RetentionPolicy.RUNTIME) //RUNTIME: аннотация сохраняется в файле .class во время компиляции и доступна через JVM во время выполнения
@Constraint (validatedBy = DateIsAfterThisValidator.class)
@Past
public @interface DateIsAfter {
	String message() default "Дата релиза должна быть не ранее {value}";

	Class<?>[] groups() default {}; //сужает действие аннотации к заданному классу

	Class<? extends Payload>[] payload() default {};

	String value() default "1895-12-28";
}
