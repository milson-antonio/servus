package com.milsondev.servus.dto.validator;

import com.milsondev.servus.dto.UserDTO;
import com.milsondev.servus.service.OrchestrationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.regex.Pattern;

public class UserDTOValidatorImpl implements ConstraintValidator<UserDTOValidator, UserDTO> {

    private final OrchestrationService orchestrationService;
    private final MessageSource messageSource;

    @Autowired
    public UserDTOValidatorImpl(OrchestrationService orchestrationService, MessageSource messageSource) {
        this.orchestrationService = orchestrationService;
        this.messageSource = messageSource;
    }

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean isValid(UserDTO dto, ConstraintValidatorContext ctx) {
        if (dto == null) {
            return false;
        }

        boolean valid = true;
        boolean isCreate = dto.getId() == null;

        // fullName required (non-null, non-blank)
        if (isBlank(dto.getFullName())) {
            addViolation(ctx, "fullName", msg("validation.signup.fullName.required"));
            valid = false;
        }

        // email required and valid format
        if (isBlank(dto.getEmail())) {
            addViolation(ctx, "email", msg("validation.signup.email.required"));
            valid = false;
        } else if (!EMAIL_REGEX.matcher(dto.getEmail().trim()).matches()) {
            addViolation(ctx, "email", msg("validation.signup.email.invalid"));
            valid = false;
        }

        // password rules
        if (isCreate) {
            if (isBlank(dto.getPassword())) {
                addViolation(ctx, "password", msg("validation.signup.password.required"));
                valid = false;
            }
        }

        // confirm password must match when password provided (create or update)
        if (!isBlank(dto.getPassword())) {
            if (dto.getConfirmPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
                addViolation(ctx, "confirmPassword", msg("validation.signup.confirmPassword.mismatch"));
                valid = false;
            }
        }

        if (dto.getEmail() != null && orchestrationService.isUserPresent(dto.getEmail())) {
            addViolation(ctx, "email", msg("validation.signup.email.exists", dto.getEmail()));
            valid = false;
        }

        return valid;
    }

    private String msg(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    private void addViolation(ConstraintValidatorContext ctx, String field, String message) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
