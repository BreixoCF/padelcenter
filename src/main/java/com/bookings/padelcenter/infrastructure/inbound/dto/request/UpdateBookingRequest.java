package com.bookings.padelcenter.infrastructure.inbound.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateBookingRequest(
	@NotBlank(message = "Start time is required")
	String startTime,

	@NotBlank(message = "End time is required")
	String endTime,

	@NotNull(message = "Total price is required")
	@Positive(message = "Total price must be positive")
	BigDecimal totalPrice
) {}
