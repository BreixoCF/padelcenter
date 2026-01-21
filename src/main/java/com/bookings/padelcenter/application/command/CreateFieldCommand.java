package com.bookings.padelcenter.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateFieldCommand(
	String name,
	String type,
	BigDecimal pricePerHour,
	Boolean isAvailable,
	UUID centerId
) {}
