package com.bookings.padelcenter.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record Booking(
	Long id,
	User user,
	Field field,
	LocalDateTime startTime,
	LocalDateTime endTime,
	BigDecimal totalPrice,
	Instant bookedAt,
	BookingStatus status,
	Auditable audit
) {
	public Booking updateDetails(LocalDateTime startTime, LocalDateTime endTime, BigDecimal totalPrice, UUID modifiedBy) {
		return new Booking(
			this.id,
			this.user,
			this.field,
			startTime,
			endTime,
			totalPrice,
			this.bookedAt,
			this.status,
			this.audit.update(modifiedBy)
		);
	}

	public Booking cancel(UUID modifiedBy) {
		return new Booking(
			this.id,
			this.user,
			this.field,
			this.startTime,
			this.endTime,
			this.totalPrice,
			this.bookedAt,
			BookingStatus.CANCELLED,
			this.audit.update(modifiedBy)
		);
	}

	public boolean isCancelled() {
		return this.status == BookingStatus.CANCELLED;
	}
}
