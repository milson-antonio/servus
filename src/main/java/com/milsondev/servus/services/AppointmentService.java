package com.milsondev.servus.services;

import com.milsondev.servus.db.entities.AppointmentEntity;
import com.milsondev.servus.db.repositories.AppointmentRepository;
import com.milsondev.servus.dtos.AppointmentDTO;
import com.milsondev.servus.enums.ApplicantType;
import com.milsondev.servus.enums.AppointmentServiceType;
import com.milsondev.servus.enums.AppointmentStatus;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final Validator validator;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              Validator validator) {
        this.appointmentRepository = appointmentRepository;
        this.validator = validator;
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

    public AppointmentEntity createForUser(UUID userId, AppointmentServiceType service,
                                           ApplicantType applicantType,
                                           Date startAt, Date endAt) {
        // default applicant type
        if (applicantType == null) {
            applicantType = ApplicantType.SELF;
        }
        // default endAt: +30 minutes
        if (startAt != null && endAt == null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(startAt);
            cal.add(java.util.Calendar.MINUTE, 30);
            endAt = cal.getTime();
        }
        // Build DTO to reuse validator rules
        AppointmentDTO dto = new AppointmentDTO();
        dto.setUserId(userId);
        dto.setAppointmentServiceType(service);
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
        entity.setAppointmentServiceType(service);
        entity.setApplicantType(applicantType);
        entity.setStartAt(dto.getStartAt());
        entity.setEndAt(dto.getEndAt());
        entity.setStatus(AppointmentStatus.Scheduled);
        return appointmentRepository.save(entity);
    }

    public void deleteAllAppointments(){
        appointmentRepository.deleteAll();
    }
}
