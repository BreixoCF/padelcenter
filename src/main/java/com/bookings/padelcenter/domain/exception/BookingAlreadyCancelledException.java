package com.bookings.padelcenter.domain.exception;

public class BookingAlreadyCancelledException extends RuntimeException {
	public BookingAlreadyCancelledException(Long bookingId) {
		super("Booking with ID " + bookingId + " is already cancelled");
	}
}
