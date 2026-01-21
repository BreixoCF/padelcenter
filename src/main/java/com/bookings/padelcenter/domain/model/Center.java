package com.bookings.padelcenter.domain.model;

import java.util.UUID;

public record Center(
	UUID id,
	String name,
	String address,
	String city,
	String phoneNumber,
	String email,
	User manager,
	Auditable audit
) {}
