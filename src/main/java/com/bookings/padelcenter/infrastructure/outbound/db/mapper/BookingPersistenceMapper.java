package com.bookings.padelcenter.infrastructure.outbound.db.mapper;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.BookingStatus;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.BookingEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class BookingPersistenceMapper {

	private final UserPersistenceMapper userPersistenceMapper;
	private final FieldPersistenceMapper fieldPersistenceMapper;
	private final AuditablePersistenceMapper auditablePersistenceMapper;

	public BookingEntity toEntity(Booking booking) {
		var entity = new BookingEntity();
		if (booking.id() != null) {
			entity.setId(booking.id());
		}
		entity.setUser(userPersistenceMapper.toEntity(booking.user()));
		entity.setField(fieldPersistenceMapper.toEntity(booking.field()));
		entity.setStartTime(parseToInstant(booking.startTime()));
		entity.setEndTime(parseToInstant(booking.endTime()));
		entity.setTotalPrice(booking.totalPrice());
		entity.setBookedAt(booking.bookedAt());
		entity.setStatus(booking.status().getId());
		auditablePersistenceMapper.mapToEntity(booking.audit(), entity);
		return entity;
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
			entity.getId(),
			user,
			field,
			formatToString(entity.getStartTime()),
			formatToString(entity.getEndTime()),
			entity.getTotalPrice(),
			entity.getBookedAt(),
			BookingStatus.fromId(entity.getStatus()),
			auditable
		);
	}

	private Instant parseToInstant(String dateTimeString) {
		if (dateTimeString == null) {
			return null;
		}
		return Instant.parse(dateTimeString);
	}

	private String formatToString(Instant instant) {
		if (instant == null) {
			return null;
		}
		return DateTimeFormatter.ISO_INSTANT.format(instant);
	}
}
