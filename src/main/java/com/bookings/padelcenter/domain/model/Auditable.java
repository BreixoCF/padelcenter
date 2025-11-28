package com.bookings.padelcenter.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Auditable(
		UUID createdBy,
		Instant createdAt,
		UUID modifiedBy,
		Instant modifiedAt,
		UUID deletedBy,
		Instant deletedAt
) {
	public static Auditable newAudit() {
		return new Auditable(
				null, Instant.now(),null,
				null, null,null
		);
	}
}
