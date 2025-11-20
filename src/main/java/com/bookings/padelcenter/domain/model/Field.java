package com.bookings.padelcenter.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Field(
	Long id,
	String number,
	String type,
	BigDecimal pricePerHour,
	Center center,
	Boolean isAvailable,
	String createdBy,
	Instant createdAt,
	String lastModifiedBy,
	Instant lastModifiedAt,
	String deletedBy,
	Instant deletedAt
) {}
