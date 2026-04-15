package com.bookings.padelcenter.application.command;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;

import java.util.UUID;

public record DeleteUserCommand(
	UUID userId,
	AuthenticatedUser authenticatedUser
) {}
