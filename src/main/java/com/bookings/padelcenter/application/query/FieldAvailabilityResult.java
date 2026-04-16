package com.bookings.padelcenter.application.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FieldAvailabilityResult(
	UUID fieldId,
	LocalDate date,
	List<TimeSlot> slots
) {
	public record TimeSlot(
		LocalDateTime start,
		LocalDateTime end,
		boolean available
	) {}
}
