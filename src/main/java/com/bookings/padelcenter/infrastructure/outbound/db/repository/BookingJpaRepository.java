package com.bookings.padelcenter.infrastructure.outbound.db.repository;

import com.bookings.padelcenter.infrastructure.outbound.db.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingJpaRepository extends JpaRepository<BookingEntity, Long> {

	Page<BookingEntity> findByUser_UserId(UUID userId, Pageable pageable);

	@Query("""
		SELECT b FROM BookingEntity b
		WHERE b.user.userId = :userId
		  AND (:status IS NULL OR b.status = :status)
		ORDER BY b.startTime DESC
		""")
	Page<BookingEntity> findByUserIdAndStatus(
		@Param("userId") UUID userId,
		@Param("status") Integer status,
		Pageable pageable
	);

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

	@Query("""
		SELECT b FROM BookingEntity b
		WHERE b.field.fieldId = :fieldId
		  AND b.status = 2
		  AND b.startTime < :dayEnd
		  AND b.endTime > :dayStart
		ORDER BY b.startTime ASC
		""")
	List<BookingEntity> findConfirmedByFieldAndDay(
		@Param("fieldId") UUID fieldId,
		@Param("dayStart") Instant dayStart,
		@Param("dayEnd") Instant dayEnd
	);

	@Query("""
		SELECT b FROM BookingEntity b
		WHERE b.field.center.centerId = :centerId
		  AND (:fieldId IS NULL OR b.field.fieldId = :fieldId)
		  AND (:dayStart IS NULL OR b.startTime >= :dayStart)
		  AND (:dayEnd IS NULL OR b.startTime < :dayEnd)
		  AND (:status IS NULL OR b.status = :status)
		ORDER BY b.startTime ASC
		""")
	Page<BookingEntity> findByCenterWithFilters(
		@Param("centerId") UUID centerId,
		@Param("fieldId") UUID fieldId,
		@Param("dayStart") Instant dayStart,
		@Param("dayEnd") Instant dayEnd,
		@Param("status") Integer status,
		Pageable pageable
	);
}
