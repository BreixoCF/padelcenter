package com.bookings.padelcenter.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CenterRole(
	UUID centerId,
	Role role,
	String createdBy,
	Instant createdAt,
	String lastModifiedBy,
	Instant lastModifiedAt,
	String deletedBy,
	Instant deletedAt
) {}
