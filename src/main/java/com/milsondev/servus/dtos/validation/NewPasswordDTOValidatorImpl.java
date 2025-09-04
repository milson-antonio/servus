package com.milsondev.servus.dtos.validation;

import com.milsondev.servus.dtos.NewPasswordDTO;
import com.milsondev.servus.services.OrchestrationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class NewPasswordDTOValidatorImpl implements ConstraintValidator<NewPasswordDTOValidator, NewPasswordDTO> {

    private final OrchestrationService orchestrationService;
    private final MessageSource messageSource;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public NewPasswordDTOValidatorImpl(OrchestrationService orchestrationService, MessageSource messageSource,
                                       PasswordEncoder passwordEncoder) {
        this.orchestrationService = orchestrationService;
        this.messageSource = messageSource;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean isValid(NewPasswordDTO dto, ConstraintValidatorContext ctx) {
        if (dto == null) {
            return false;
        }
        boolean valid = true;

        // Validate token decodability and check same-as-old password
        String token = orchestrationService.safeTrim(dto.getToken());
        String email = null;
        if (orchestrationService.isBlank(token)) {
            addViolation(ctx, "token", msg("validation.reset.token.required"));
            valid = false;
        } else {
            try {
                email = orchestrationService.getEmailFromToken(token);
                if (email == null || email.isBlank()) {
                    addViolation(ctx, "token", msg("validation.reset.token.invalid"));
                    valid = false;
                }
            } catch (Exception ex) {
                addViolation(ctx, "token", msg("validation.reset.token.invalid"));
                valid = false;
            }
        }

        // Password required and min length + confirm required and match
        boolean passwordProvided = !orchestrationService.isBlank(dto.getPassword());
        boolean confirmProvided = !orchestrationService.isBlank(dto.getConfirmPassword());

        if (!passwordProvided) {
            addViolation(ctx, "password", msg("validation.reset.password.required"));
            valid = false;
        } else if (dto.getPassword().length() < 8) {
            addViolation(ctx, "password", msg("validation.reset.password.size"));
            valid = false;
        } else if (!orchestrationService.isStrongPassword(dto.getPassword())) {
            addViolation(ctx, "password", msg("validation.reset.password.weak"));
            valid = false;
        }

        if (!confirmProvided) {
            addViolation(ctx, "confirmPassword", msg("validation.reset.confirm.required"));
            valid = false;
        }

        if (passwordProvided && confirmProvided && !dto.getPassword().equals(dto.getConfirmPassword())) {
            addViolation(ctx, "confirmPassword", msg("validation.reset.confirm.mismatch"));
            valid = false;
        }

        // Check new password is not same as old (only if token/email valid and user exists and password provided)
        if (passwordProvided && email != null && !email.isBlank()) {
            var userOpt = orchestrationService.findUserByEmail(email);
            if (userOpt.isPresent()) {
                if (passwordEncoder.matches(dto.getPassword(), userOpt.get().getPassword())) {
                    addViolation(ctx, "password", msg("validation.reset.password.sameAsOld"));
                    valid = false;
                }
            }
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