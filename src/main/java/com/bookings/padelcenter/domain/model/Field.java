package com.bookings.padelcenter.domain.model;

import java.math.BigDecimal;

public record Field(
	Long id,
	String number,
	String type,
	BigDecimal pricePerHour,
	Center center,
	Boolean isAvailable,
	Auditable audit
) {}
