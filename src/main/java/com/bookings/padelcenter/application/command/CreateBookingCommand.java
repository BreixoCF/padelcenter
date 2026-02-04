package com.bookings.padelcenter.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBookingCommand(
	UUID userId,
	UUID fieldId,
	String startTime,
	String endTime,
	BigDecimal totalPrice
) {}
