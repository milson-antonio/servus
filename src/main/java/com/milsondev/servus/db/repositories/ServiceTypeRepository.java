package com.milsondev.servus.db.repositories;

import com.milsondev.servus.db.entities.ServiceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, UUID> {
    Optional<ServiceTypeEntity> findByName(String name);
}
