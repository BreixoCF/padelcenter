package com.bookings.padelcenter.infrastructure.inbound.dto.response;

public record CenterSummaryResponse(
	String name,
	String address,
	String city
) {}
