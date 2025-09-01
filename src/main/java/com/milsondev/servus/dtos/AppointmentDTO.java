package com.milsondev.servus.dtos;

import com.milsondev.servus.dtos.validation.AppointmentDTOValidator;
import com.milsondev.servus.enums.ApplicantType;
import com.milsondev.servus.enums.AppointmentStatus;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AppointmentDTOValidator
public class AppointmentDTO {
    private UUID id;
    private UUID userId;
    private String service;
    private ApplicantType applicantType = ApplicantType.SELF;
    private Date startAt;
    private Date endAt; // optional; if null, validator may derive a default duration
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
}
