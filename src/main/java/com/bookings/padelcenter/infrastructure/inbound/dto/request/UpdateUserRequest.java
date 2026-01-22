package com.bookings.padelcenter.infrastructure.inbound.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
	@NotBlank(message = "First name must not be blank")
	String firstName,
	@NotBlank(message = "Last name must not be blank")
	String lastName,
	@NotBlank(message = "Email must not be blank")
	String email,
	@Size(min = 8, message = "Password must be at least 8 characters long")
	String password,
	String phoneNumber
) {}
