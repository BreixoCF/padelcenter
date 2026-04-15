package com.bookings.padelcenter.application.command;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;

public record CancelBookingCommand(
	Long bookingId,
	AuthenticatedUser authenticatedUser
) {}
