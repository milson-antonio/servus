package com.milsondev.servus.dtos.validation;

import com.milsondev.servus.dtos.UserDTO;
import com.milsondev.servus.services.OrchestrationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserDTOValidatorImpl implements ConstraintValidator<UserDTOValidator, UserDTO> {

    private final OrchestrationService orchestrationService;
    private final MessageSource messageSource;

    @Autowired
    public UserDTOValidatorImpl(OrchestrationService orchestrationService, MessageSource messageSource) {
        this.orchestrationService = orchestrationService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean isValid(UserDTO dto, ConstraintValidatorContext ctx) {
        if (dto == null) {
            return false;
        }

        boolean valid = true;
        boolean isCreate = dto.getId() == null;

        // email required and valid format
        if (orchestrationService.isBlank(dto.getEmail())) {
            addViolation(ctx, "email", msg("validation.signup.email.required"));
            valid = false;
        } else if (!orchestrationService.isValidEmailFormat(dto.getEmail())) {
            addViolation(ctx, "email", msg("validation.signup.email.invalid"));
            valid = false;
        }

        // password rules
        if (isCreate) {
            if (orchestrationService.isBlank(dto.getPassword())) {
                addViolation(ctx, "password", msg("validation.signup.password.required"));
                valid = false;
            } else if (!orchestrationService.isStrongPassword(dto.getPassword())) {
                addViolation(ctx, "password", msg("validation.signup.password.weak"));
                valid = false;
            }
        } else {
            // update: if password provided, validate strength too
            if (!orchestrationService.isBlank(dto.getPassword()) && !orchestrationService.isStrongPassword(dto.getPassword())) {
                addViolation(ctx, "password", msg("validation.signup.password.weak"));
                valid = false;
            }
        }

        // confirm password must match when password provided (create or update)
        if (!orchestrationService.isBlank(dto.getPassword())) {
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
}
