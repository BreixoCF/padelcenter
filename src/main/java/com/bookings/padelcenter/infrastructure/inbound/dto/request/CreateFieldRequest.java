package com.bookings.padelcenter.infrastructure.inbound.dto.request;

import java.math.BigDecimal;

public record CreateFieldRequest(
	String name,
	String type,
	BigDecimal pricePerHour,
	Boolean isAvailable
) {}
