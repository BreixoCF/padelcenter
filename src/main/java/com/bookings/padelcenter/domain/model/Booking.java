package com.bookings.padelcenter.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

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
) {}
