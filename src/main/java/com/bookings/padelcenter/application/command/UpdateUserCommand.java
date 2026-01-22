package com.bookings.padelcenter.application.command;

import java.util.UUID;

public record UpdateUserCommand(
	UUID id,
	String firstName,
	String lastName,
	String email,
	String password,
	String phoneNumber
) {}
