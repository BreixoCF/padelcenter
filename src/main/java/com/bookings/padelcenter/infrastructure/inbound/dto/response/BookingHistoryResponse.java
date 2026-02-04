package com.bookings.padelcenter.infrastructure.inbound.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingHistoryResponse(
	Long id,
	FieldSummaryResponse field,
	String startTime,
	String endTime,
	BigDecimal totalPrice,
	Instant bookedAt,
	String status
) {}
