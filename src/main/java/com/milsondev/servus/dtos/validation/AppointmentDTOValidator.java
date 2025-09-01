package com.milsondev.servus.dtos.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AppointmentDTOValidatorImpl.class)
public @interface AppointmentDTOValidator {
    String message() default "Appointment not valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
