package com.bookings.padelcenter.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateBookingCommand(
	Long bookingId,
	String startTime,
	String endTime,
	BigDecimal totalPrice,
	UUID modifiedBy
) {}
