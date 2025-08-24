package com.milsondev.servus.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserDTOValidatorImpl.class)
public @interface UserDTOValidator {
    String message() default "User not valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}