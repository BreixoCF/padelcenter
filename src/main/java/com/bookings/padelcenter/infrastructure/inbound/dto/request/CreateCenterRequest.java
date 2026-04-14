package com.bookings.padelcenter.infrastructure.inbound.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCenterRequest(
	@NotBlank(message = "Name is required")
	@Size(max = 255, message = "Name must not exceed 255 characters")
	String name,

	@NotBlank(message = "Address is required")
	@Size(max = 255, message = "Address must not exceed 255 characters")
	String address,

	@NotBlank(message = "City is required")
	@Size(max = 100, message = "City must not exceed 100 characters")
	String city,

	@Size(max = 20, message = "Phone number must not exceed 20 characters")
	String phoneNumber,

	@Email(message = "Email must be valid")
	@Size(max = 255, message = "Email must not exceed 255 characters")
	String email
) {}
