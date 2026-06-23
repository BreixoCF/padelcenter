package com.bookings.padelcenter.infrastructure.outbound.db.repository;

import com.bookings.padelcenter.infrastructure.outbound.db.entity.FieldEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FieldJpaRepository extends JpaRepository<FieldEntity, UUID> {

	List<FieldEntity> findByCenterCenterId(UUID centerId);

	@Query("""
		SELECT f FROM FieldEntity f
		WHERE f.center.centerId = :centerId
		  AND (:type IS NULL OR f.type = :type)
		  AND (:available IS NULL OR f.isAvailable = :available)
		""")
	Page<FieldEntity> findByCenterWithFilters(
		@Param("centerId") UUID centerId,
		@Param("type") String type,
		@Param("available") Boolean available,
		Pageable pageable
	);
}
