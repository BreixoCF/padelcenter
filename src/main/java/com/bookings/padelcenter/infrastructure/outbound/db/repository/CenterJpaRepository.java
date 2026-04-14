package com.bookings.padelcenter.infrastructure.outbound.db.repository;

import com.bookings.padelcenter.infrastructure.outbound.db.entity.CenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CenterJpaRepository extends JpaRepository<CenterEntity, UUID> {

	Optional<CenterEntity> findByName(String name);

	boolean existsByName(String name);
}
