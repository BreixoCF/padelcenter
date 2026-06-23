package com.bookings.padelcenter.infrastructure.outbound.db.mapper;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.BookingStatus;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.BookingEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class BookingPersistenceMapper {

	private static final ZoneId ZONE = ZoneId.of("UTC");

	private final UserPersistenceMapper userPersistenceMapper;
	private final FieldPersistenceMapper fieldPersistenceMapper;
	private final AuditablePersistenceMapper auditablePersistenceMapper;

	public BookingEntity toEntity(Booking booking) {
		var entity = new BookingEntity();
		if (booking.bookingId() != null) {
			entity.setBookingId(booking.bookingId());
		}
		updateEntity(entity, booking);
		return entity;
	}

	public void updateEntity(BookingEntity entity, Booking booking) {
		entity.setUser(userPersistenceMapper.toEntity(booking.user()));
		entity.setField(fieldPersistenceMapper.toEntity(booking.field()));
		entity.setStartTime(toInstant(booking.startTime()));
		entity.setEndTime(toInstant(booking.endTime()));
		entity.setTotalPrice(booking.totalPrice());
		entity.setBookedAt(booking.bookedAt());
		entity.setStatus(booking.status().getId());
		auditablePersistenceMapper.mapToEntity(booking.audit(), entity);
	}

	public Booking toDomain(BookingEntity entity) {
		var user = userPersistenceMapper.toDomain(entity.getUser());
		var field = fieldPersistenceMapper.toDomain(entity.getField());
		var auditable = new Auditable(
			entity.getCreatedBy(),
			entity.getCreatedAt(),
			entity.getModifiedBy(),
			entity.getModifiedAt(),
			entity.getDeletedBy(),
			entity.getDeletedAt()
		);
		return new Booking(
			entity.getBookingId(),
			user,
			field,
			toLocalDateTime(entity.getStartTime()),
			toLocalDateTime(entity.getEndTime()),
			entity.getTotalPrice(),
			entity.getBookedAt(),
			BookingStatus.fromId(entity.getStatus()),
			auditable
		);
	}

	private Instant toInstant(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}
		return localDateTime.atZone(ZONE).toInstant();
	}

	private LocalDateTime toLocalDateTime(Instant instant) {
		if (instant == null) {
			return null;
		}
		return instant.atZone(ZONE).toLocalDateTime();
	}
}
