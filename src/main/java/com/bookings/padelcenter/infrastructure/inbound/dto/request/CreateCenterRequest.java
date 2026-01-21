package com.bookings.padelcenter.infrastructure.inbound.dto.request;

public record CreateCenterRequest(
	String name,
	String address,
	String city,
	String phoneNumber,
	String email
) {}
