package com.bookings.padelcenter.application.command;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;

import java.util.UUID;

public record DeleteCenterCommand(
	UUID centerId,
	AuthenticatedUser authenticatedUser
) {}
