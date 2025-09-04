package com.milsondev.servus.dtos.validation;

import com.milsondev.servus.db.entities.UserEntity;
import com.milsondev.servus.db.repositories.AppointmentRepository;
import com.milsondev.servus.db.repositories.UserRepository;
import com.milsondev.servus.dtos.AppointmentDTO;
import com.milsondev.servus.enums.AppointmentStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
@Component
public class AppointmentDTOValidatorImpl implements ConstraintValidator<AppointmentDTOValidator, AppointmentDTO> {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final MessageSource messageSource;
    private final ValidationService validationService;

    @Autowired
    public AppointmentDTOValidatorImpl(UserRepository userRepository,
                                       AppointmentRepository appointmentRepository,
                                       MessageSource messageSource,
                                       ValidationService validationService) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.messageSource = messageSource;
        this.validationService = validationService;
    }

    @Override
    public boolean isValid(AppointmentDTO dto, ConstraintValidatorContext ctx) {
        if (dto == null) return false;
        boolean valid = true;

        // userId must exist
        UUID userId = dto.getUserId();
        Optional<UserEntity> userOpt = Optional.empty();
        if (userId == null) {
            addViolation(ctx, "userId", msg("validation.appointment.user.required"));
            valid = false;
        } else {
            userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                addViolation(ctx, "userId", msg("validation.appointment.user.notfound"));
                valid = false;
            }
        }

        // service required
        if (dto.getAppointmentServiceType() == null) {
            addViolation(ctx, "service", msg("validation.appointment.service.required"));
            valid = false;
        }

        // startAt must be in the future
        Date now = new Date();
        Date startAt = dto.getStartAt();
        Date endAt = dto.getEndAt();
        if (startAt == null) {
            addViolation(ctx, "startAt", msg("validation.appointment.startAt.required"));
            valid = false;
        } else if (!startAt.after(now)) {
            addViolation(ctx, "startAt", msg("validation.appointment.startAt.future"));
            valid = false;
        }

        // endAt must be > startAt; if null, derive +30min for validation purposes
        if (startAt != null) {
            if (endAt == null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(startAt);
                cal.add(Calendar.MINUTE, 30);
                endAt = cal.getTime();
                dto.setEndAt(endAt); // provide default duration
            }
            if (!endAt.after(startAt)) {
                addViolation(ctx, "endAt", msg("validation.appointment.endAt.afterStart"));
                valid = false;
            }
        }

        // overlap check if we have user and both times
        /*
        if (valid && userOpt.isPresent() && startAt != null && endAt != null) {
            boolean overlap = appointmentRepository.existsOverlap(userId, startAt, endAt, AppointmentStatus.CANCELLED);
            if (overlap) {
                addViolation(ctx, "startAt", msg("validation.appointment.overlap"));
                valid = false;
            }
        }
         */

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
