package com.bookings.padelcenter.application.command;

import java.util.UUID;

public record CancelBookingCommand(
	Long bookingId,
	UUID modifiedBy
) {}
