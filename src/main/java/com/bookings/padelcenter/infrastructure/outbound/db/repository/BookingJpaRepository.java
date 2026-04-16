package com.bookings.padelcenter.infrastructure.outbound.db.repository;

import com.bookings.padelcenter.infrastructure.outbound.db.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface BookingJpaRepository extends JpaRepository<BookingEntity, Long> {

	Page<BookingEntity> findByUser_UserId(UUID userId, Pageable pageable);

	@Query("""
		SELECT COUNT(b) > 0 FROM BookingEntity b
		WHERE b.field.fieldId = :fieldId
		  AND b.status IN (1, 2)
		  AND b.startTime < :end
		  AND b.endTime > :start
		  AND (:excludeId IS NULL OR b.bookingId <> :excludeId)
		""")
	boolean existsOverlappingBooking(
		@Param("fieldId") UUID fieldId,
		@Param("start") Instant start,
		@Param("end") Instant end,
		@Param("excludeId") Long excludeId
	);
}
