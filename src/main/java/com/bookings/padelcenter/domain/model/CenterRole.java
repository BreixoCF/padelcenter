package com.bookings.padelcenter.domain.model;

import java.util.UUID;

public record CenterRole(
	UUID centerId,
	Role role,
	Auditable audit
) {
	public CenterRole delete(UUID deletedBy) {
		return new CenterRole(
			this.centerId,
			this.role,
			this.audit.delete(deletedBy)
		);
	}
}
