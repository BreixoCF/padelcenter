package com.bookings.padelcenter.domain.model;

import java.time.Instant;

public record Center(
	Long id,
	String name,
	String address,
	String phoneNumber,
	String email,
	String createdBy,
	Instant createdAt,
	String lastModifiedBy,
	Instant lastModifiedAt,
	String deletedBy,
	Instant deletedAt
) {}
