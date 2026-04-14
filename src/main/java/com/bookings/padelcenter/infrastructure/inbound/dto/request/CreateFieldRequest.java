package com.bookings.padelcenter.infrastructure.inbound.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateFieldRequest(
	@NotBlank(message = "Name is required")
	@Size(max = 50, message = "Name must not exceed 50 characters")
	String name,

	@Size(max = 50, message = "Type must not exceed 50 characters")
	String type,

	@NotNull(message = "Price per hour is required")
	@Positive(message = "Price per hour must be positive")
	BigDecimal pricePerHour,

	Boolean isAvailable
) {}
