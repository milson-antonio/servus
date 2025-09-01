package com.milsondev.servus.services;

import com.milsondev.servus.db.entities.AppointmentEntity;
import com.milsondev.servus.db.repositories.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final jakarta.validation.Validator validator;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
    }

    public List<AppointmentEntity> listAllByUser(UUID userId) {
        return appointmentRepository.findByUserIdOrderByStartAtAsc(userId);
    }

    public List<AppointmentEntity> listUpcomingByUser(UUID userId) {
        Date now = new Date();
        return appointmentRepository.findByUserIdOrderByStartAtAsc(userId)
                .stream()
                .filter(a -> a.getStartAt() != null && a.getStartAt().after(now))
                .collect(Collectors.toList());
    }

    public List<AppointmentEntity> listPastByUser(UUID userId) {
        Date now = new Date();
        return appointmentRepository.findByUserIdOrderByStartAtAsc(userId)
                .stream()
                .filter(a -> a.getStartAt() != null && !a.getStartAt().after(now))
                .collect(Collectors.toList());
    }

    public AppointmentEntity createForUser(UUID userId, String service, com.milsondev.servus.enums.ApplicantType applicantType,
                                           Date startAt, Date endAt) {
        // default applicant type
        if (applicantType == null) {
            applicantType = com.milsondev.servus.enums.ApplicantType.SELF;
        }
        // default endAt: +30 minutes
        if (startAt != null && endAt == null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(startAt);
            cal.add(java.util.Calendar.MINUTE, 30);
            endAt = cal.getTime();
        }
        // Build DTO to reuse validator rules
        com.milsondev.servus.dtos.AppointmentDTO dto = new com.milsondev.servus.dtos.AppointmentDTO();
        dto.setUserId(userId);
        dto.setService(service);
        dto.setApplicantType(applicantType);
        dto.setStartAt(startAt);
        dto.setEndAt(endAt);

        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String msg = violations.stream().map(v -> v.getPropertyPath() + ": " + v.getMessage()).reduce((a,b) -> a + "; " + b).orElse("Invalid appointment");
            throw new IllegalArgumentException(msg);
        }

        AppointmentEntity entity = new AppointmentEntity();
        entity.setUserId(userId);
        entity.setService(service);
        entity.setApplicantType(applicantType);
        entity.setStartAt(dto.getStartAt());
        entity.setEndAt(dto.getEndAt());
        entity.setStatus(com.milsondev.servus.enums.AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(entity);
    }
}
