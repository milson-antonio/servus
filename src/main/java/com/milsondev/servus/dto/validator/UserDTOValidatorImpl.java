package com.milsondev.servus.dto.validator;

import com.milsondev.servus.dto.UserDTO;
import com.milsondev.servus.service.OrchestrationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Pattern;

public class UserDTOValidatorImpl implements ConstraintValidator<UserDTOValidator, UserDTO> {

    private final OrchestrationService orchestrationService;

    @Autowired
    public UserDTOValidatorImpl(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
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
            addViolation(ctx, "fullName", "Nome completo é obrigatório");
            valid = false;
        }

        // email required and valid format
        if (isBlank(dto.getEmail())) {
            addViolation(ctx, "email", "Email é obrigatório");
            valid = false;
        } else if (!EMAIL_REGEX.matcher(dto.getEmail().trim()).matches()) {
            addViolation(ctx, "email", "Email inválido");
            valid = false;
        }

        // password rules
        if (isCreate) {
            if (isBlank(dto.getPassword())) {
                addViolation(ctx, "password", "Palavra‑passe é obrigatória");
                valid = false;
            }
        }

        // confirm password must match when password provided (create or update)
        if (!isBlank(dto.getPassword())) {
            if (dto.getConfirmPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
                addViolation(ctx, "confirmPassword", "As palavras‑passe não coincidem");
                valid = false;
            }
        }

        if(orchestrationService.isUserPresent(dto.getEmail())) {
            addViolation(ctx, "e-mail", "Ja existe uma conta com este e-mail: " + dto.getEmail());
            valid = false;
        }



        return valid;
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
