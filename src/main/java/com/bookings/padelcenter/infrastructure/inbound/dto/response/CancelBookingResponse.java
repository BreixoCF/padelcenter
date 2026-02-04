package com.bookings.padelcenter.infrastructure.inbound.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CancelBookingResponse(
	Long id,
	String status,
	String message
) {}
