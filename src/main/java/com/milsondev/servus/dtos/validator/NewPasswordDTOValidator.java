package com.milsondev.servus.dtos.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NewPasswordDTOValidatorImpl.class)
public @interface NewPasswordDTOValidator {
    String message() default "New password data not valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
