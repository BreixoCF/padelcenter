package com.bookings.padelcenter.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Booking(
	Long id,
	User user,
	Field field,
	String startTime,
	String endTime,
	BigDecimal totalPrice,
	Instant bookedAt,
	BookingStatus status,
	String createdBy,
	Instant createdAt,
	String lastModifiedBy,
	Instant lastModifiedAt,
	String deletedBy,
	Instant deletedAt
) {}
