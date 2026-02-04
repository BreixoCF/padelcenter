package com.bookings.padelcenter.infrastructure.inbound.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateBookingResponse(
	Long id,
	UUID userId,
	UUID fieldId,
	String startTime,
	String endTime,
	BigDecimal totalPrice,
	Instant bookedAt,
	String status,
	AuditableResponse audit
) {}
