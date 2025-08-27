package com.milsondev.servus.db.repositories;

import com.milsondev.servus.db.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository <UserEntity, UUID>{
    Optional<UserEntity> findByEmail(String email);
}