package com.milsondev.servus.db.entities;

import com.milsondev.servus.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "user", schema = "servus")
public class UserEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, length = 100, nullable = false, name = "email")
    private String email;

    @Column(length = 20, name = "phone")
    private String phone;

    @Column(nullable = false, name = "password")
    private String password;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "active")
    private boolean active;

    // Token version for invalidating JWTs on password reset or other events
    @Column(name = "token_version")
    private Integer tokenVersion = 0;

    // Rate limiting: last time a password reset email was requested
    @Column(name = "last_password_reset_request_at")
    private Date lastPasswordResetRequestAt;
}
