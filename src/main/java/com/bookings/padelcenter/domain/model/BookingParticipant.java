package com.bookings.padelcenter.domain.model;

public record BookingParticipant(
	Long bookingParticipantId,
	Booking booking,
	User user
) {}
