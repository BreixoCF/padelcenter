package com.bookings.padelcenter.infrastructure.outbound.db.repository;

import com.bookings.padelcenter.infrastructure.outbound.db.entity.TournamentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TournamentJpaRepository extends JpaRepository<TournamentEntity, UUID> {

	Page<TournamentEntity> findByCenterId(UUID centerId, Pageable pageable);
}
