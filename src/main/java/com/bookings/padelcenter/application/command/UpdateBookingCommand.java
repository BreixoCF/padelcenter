package com.bookings.padelcenter.application.command;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateBookingCommand(
	Long bookingId,
	LocalDateTime startTime,
	LocalDateTime endTime,
	BigDecimal totalPrice,
	AuthenticatedUser authenticatedUser
) {}
