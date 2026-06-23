package com.bookings.padelcenter.application.command;

public record CreateCenterCommand(
	String name,
	String address,
	String city,
	String phoneNumber,
	String email
) {}
