package com.bookings.padelcenter.domain.model;

public record Center(
	Long id,
	String name,
	String address,
	String phoneNumber,
	String email,
	Auditable audit
) {}
