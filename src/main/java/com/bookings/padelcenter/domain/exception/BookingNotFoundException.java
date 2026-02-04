package com.bookings.padelcenter.domain.exception;

public class BookingNotFoundException extends ResourceNotFoundException {
	public BookingNotFoundException(Long bookingId) {
		super("Booking with ID " + bookingId + " not found");
	}
}
