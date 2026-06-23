package com.bookings.padelcenter.application.command;

import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.model.CenterRole;

import java.util.Set;
import java.util.UUID;

public record UpdateUserRolesCommand(
	UUID userId,
	Set<CenterRole> newRoles,
	AuthenticatedUser authenticatedUser
) {}
