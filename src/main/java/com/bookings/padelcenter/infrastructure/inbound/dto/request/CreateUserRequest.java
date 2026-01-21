package com.bookings.padelcenter.infrastructure.inbound.dto.request;

public record CreateUserRequest(
	String firstName,
	String lastName,
	String email,
	String password,
	String phoneNumber
) {}
