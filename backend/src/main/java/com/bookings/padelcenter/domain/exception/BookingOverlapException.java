package com.bookings.padelcenter.domain.exception;

import java.time.LocalDateTime;
import java.util.UUID;

public class BookingOverlapException extends RuntimeException {
	public BookingOverlapException(UUID fieldId, LocalDateTime start, LocalDateTime end) {
		super("Field " + fieldId + " is already booked from " + start + " to " + end);
	}
}
