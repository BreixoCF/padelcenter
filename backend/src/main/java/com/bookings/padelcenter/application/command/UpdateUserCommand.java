package com.bookings.padelcenter.application.command;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;

import java.util.UUID;

public record UpdateUserCommand(
	UUID userId,
	String firstName,
	String lastName,
	String email,
	String password,
	String phoneNumber,
	AuthenticatedUser authenticatedUser
) {}
