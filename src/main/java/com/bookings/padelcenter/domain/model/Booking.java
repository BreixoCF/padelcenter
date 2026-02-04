package com.bookings.padelcenter.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Booking(
	Long id,
	User user,
	Field field,
	String startTime,
	String endTime,
	BigDecimal totalPrice,
	Instant bookedAt,
	BookingStatus status,
	Auditable audit
) {
	public Booking updateDetails(String startTime, String endTime, BigDecimal totalPrice, UUID modifiedBy) {
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
