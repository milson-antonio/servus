package com.milsondev.servus.dtos.validator;

import com.milsondev.servus.dtos.NewPasswordDTO;
import com.milsondev.servus.services.TokenService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class NewPasswordDTOValidatorImpl implements ConstraintValidator<NewPasswordDTOValidator, NewPasswordDTO> {

    private final TokenService tokenService;
    private final MessageSource messageSource;

    @Autowired
    public NewPasswordDTOValidatorImpl(TokenService tokenService, MessageSource messageSource) {
        this.tokenService = tokenService;
        this.messageSource = messageSource;
    }

    @Override
    public boolean isValid(NewPasswordDTO dto, ConstraintValidatorContext ctx) {
        if (dto == null) {
            return false;
        }
        boolean valid = true;

        // Validate token decodability
        String token = safeTrim(dto.getToken());
        if (isBlank(token)) {
            addViolation(ctx, "token", msg("validation.reset.token.required"));
            valid = false;
        } else {
            try {
                String email = tokenService.getEmailFromToken(token);
                if (email == null || email.isBlank()) {
                    addViolation(ctx, "token", msg("validation.reset.token.invalid"));
                    valid = false;
                }
            } catch (Exception ex) {
                addViolation(ctx, "token", msg("validation.reset.token.invalid"));
                valid = false;
            }
        }

        // Confirm password must match
        if (!isBlank(dto.getPassword())) {
            if (dto.getConfirmPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
                addViolation(ctx, "confirmPassword", msg("validation.reset.confirm.mismatch"));
                valid = false;
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

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
