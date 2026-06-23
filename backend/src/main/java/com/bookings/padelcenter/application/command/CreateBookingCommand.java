package com.bookings.padelcenter.application.command;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateBookingCommand(
	UUID userId,
	UUID fieldId,
	LocalDateTime startTime,
	LocalDateTime endTime,
	BigDecimal totalPrice
) {}
