package com.bookings.padelcenter.application.command;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;

import java.util.UUID;

public record UpdatePasswordCommand(
	UUID userId,
	String currentPassword,
	String newPassword,
	AuthenticatedUser authenticatedUser
) {}
