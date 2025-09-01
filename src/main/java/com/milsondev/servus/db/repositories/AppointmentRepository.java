package com.milsondev.servus.db.repositories;

import com.milsondev.servus.db.entities.AppointmentEntity;
import com.milsondev.servus.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AppointmentEntity a " +
            "WHERE a.userId = :userId AND a.status <> :cancelled " +
            "AND a.startAt < :end AND a.endAt > :start")
    boolean existsOverlap(@Param("userId") UUID userId,
                          @Param("start") Date start,
                          @Param("end") Date end,
                          @Param("cancelled") AppointmentStatus cancelled);

    List<AppointmentEntity> findByUserIdOrderByStartAtAsc(UUID userId);
}
