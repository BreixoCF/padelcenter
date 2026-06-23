package com.bookings.padelcenter.application.command;

public record CreateUserCommand(
	String firstName,
	String lastName,
	String email,
	String password,
	String phoneNumber
) {}
