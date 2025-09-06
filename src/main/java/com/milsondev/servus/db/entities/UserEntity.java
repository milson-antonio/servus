package com.milsondev.servus.db.entities;

import com.milsondev.servus.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "user", schema = "servus")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Column(nullable = false, name = "last_name")
    private String lastName;

    @Column(unique = true, length = 100, nullable = false, name = "email")
    private String email;

    @Column(length = 20, name = "phone")
    private String phone;

    @Column(nullable = false, name = "password")
    private String password;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "passport_number")
    private String passportNumber;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name="role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "active")
    private boolean active;

    @Column(name = "token_version")
    private Integer tokenVersion = 0;

    @Column(name = "last_password_reset_request_at")
    private Date lastPasswordResetRequestAt;
}
