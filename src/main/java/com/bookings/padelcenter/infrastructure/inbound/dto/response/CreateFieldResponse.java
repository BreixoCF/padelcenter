package com.bookings.padelcenter.infrastructure.inbound.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record CreateFieldResponse(
	UUID id,
	String name,
	String type,
	BigDecimal pricePerHour,
	Boolean isAvailable,
	CenterSummaryResponse center,
	AuditableResponse audit
) {}
