package com.bookings.padelcenter.application.command;

public record SyncUserCommand(
	String keycloakId,
	String email,
	String firstName,
	String lastName
) {}
