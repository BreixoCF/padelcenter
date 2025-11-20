package com.bookings.padelcenter.domain.model;

public record BookingParticipant(
	Long id,
	Booking booking,
	User user
) {}
