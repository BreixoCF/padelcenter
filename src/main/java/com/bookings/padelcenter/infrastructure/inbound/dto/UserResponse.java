package com.bookings.padelcenter.infrastructure.inbound.dto;

import java.util.UUID;

public record UserResponse(
	UUID id,
	String firstName,
	String lastName,
	String email,
	String phoneNumber
) {}
