package com.bookings.padelcenter.application.command;

import java.util.UUID;

public record DeleteUserCommand(
	UUID userId,
	UUID deletedBy
) {}
