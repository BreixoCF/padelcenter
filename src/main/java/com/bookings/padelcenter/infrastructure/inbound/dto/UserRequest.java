package com.bookings.padelcenter.infrastructure.inbound.dto;

public record UserRequest(
	String firstName,
	String lastName,
	String email,
	String password,
	String phoneNumber
) {}
