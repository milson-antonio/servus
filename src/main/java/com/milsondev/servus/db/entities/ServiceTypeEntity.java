package com.milsondev.servus.db.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "service_type", schema = "servus")
@Data
@NoArgsConstructor
public class ServiceTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "passportRenewal"

    @Column(nullable = false)
    private String label; // e.g., "Passport Renewal"

    @Column(length = 512)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "service_type_required_documents", schema = "servus", joinColumns = @JoinColumn(name = "service_type_id"))
    @Column(name = "document")
    private List<String> requiredDocuments = new ArrayList<>();
}