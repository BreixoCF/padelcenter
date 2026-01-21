package com.bookings.padelcenter.infrastructure.outbound.db.repository;

import com.bookings.padelcenter.infrastructure.outbound.db.entity.FieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FieldJpaRepository extends JpaRepository<FieldEntity, UUID> {
}
