package com.milsondev.servus.services;

import com.milsondev.servus.db.entities.AppointmentEntity;
import com.milsondev.servus.db.entities.ServiceTypeEntity;
import com.milsondev.servus.db.repositories.AppointmentRepository;
import com.milsondev.servus.dtos.AppointmentDTO;
import com.milsondev.servus.enums.ApplicantType;
import com.milsondev.servus.enums.AppointmentStatus;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.*;
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

    public Map<String, List<String>> getAvailableSlots(int year, int month) {
        Map<String, List<String>> availableSlots = new HashMap<>();

        // Dummy data: in a real application, this would query the database.
        List<String> times = Arrays.asList(
                "09:00",
                "09:30",
                "10:00",
                "10:30",
                "11:00",
                "11:30",
                "13:00",
                "13:30",
                "14:00"
        );

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            // Appointments only available on weekdays
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                // Skip some days for demonstration
                if (day % 4 != 0) {
                    String dateStr = String.format("%d-%02d-%02d", year, month, day);
                    availableSlots.put(dateStr, times);
                }
            }
        }

        return availableSlots;
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

    public AppointmentEntity createForUser(UUID userId, ServiceTypeEntity serviceType,
                                           ApplicantType applicantType,
                                           Date startAt, Date endAt,
                                           boolean forOther, Map<String, String> otherPersonDetails) {
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

        AppointmentEntity entity = new AppointmentEntity();
        entity.setUserId(userId);
        entity.setServiceType(serviceType);
        entity.setApplicantType(applicantType);
        entity.setStartAt(startAt);
        entity.setEndAt(endAt);
        entity.setStatus(AppointmentStatus.Scheduled);
        entity.setForOther(forOther);

        if (forOther && otherPersonDetails != null && !otherPersonDetails.isEmpty()) {
            entity.setOtherPersonDetails(otherPersonDetails);
        }

        return appointmentRepository.save(entity);
    }

    public void deleteAllAppointments(){
        appointmentRepository.deleteAll();
    }
}