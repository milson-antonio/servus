package com.milsondev.servus.db.entities;

import com.milsondev.servus.enums.ApplicantType;
import com.milsondev.servus.enums.AppointmentServiceType;
import com.milsondev.servus.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "appointment", schema = "servus")
@Data
@NoArgsConstructor
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentServiceType appointmentServiceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicantType applicantType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(nullable = false)
    private Date startAt;

    @Column(nullable = false)
    private Date endAt;

    @Column(nullable = false)
    private boolean forOther = false;

    @ElementCollection
    @CollectionTable(name = "appointment_other_person_details", schema = "servus", joinColumns = @JoinColumn(name = "appointment_id"))
    @MapKeyColumn(name = "detail_key")
    @Column(name = "detail_value")
    private Map<String, String> otherPersonDetails = new HashMap<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}