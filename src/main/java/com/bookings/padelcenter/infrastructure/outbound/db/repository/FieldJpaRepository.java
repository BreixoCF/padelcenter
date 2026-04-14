package com.bookings.padelcenter.infrastructure.outbound.db.repository;

import com.bookings.padelcenter.infrastructure.outbound.db.entity.FieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FieldJpaRepository extends JpaRepository<FieldEntity, UUID> {

	List<FieldEntity> findByCenterCenterId(UUID centerId);
}
