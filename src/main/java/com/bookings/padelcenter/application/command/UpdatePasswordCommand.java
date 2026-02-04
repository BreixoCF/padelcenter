package com.bookings.padelcenter.application.command;

import java.util.UUID;

public record UpdatePasswordCommand(
	UUID userId,
	String currentPassword,
	String newPassword,
	UUID modifiedBy
) {}
