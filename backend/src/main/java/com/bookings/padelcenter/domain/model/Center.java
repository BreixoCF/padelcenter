package com.bookings.padelcenter.domain.model;

import java.util.UUID;

public record Center(
	UUID centerId,
	String name,
	String address,
	String city,
	String phoneNumber,
	String email,
	User manager,
	Auditable audit
) {
	public Center delete(UUID deletedBy) {
		return new Center(
			this.centerId,
			this.name,
			this.address,
			this.city,
			this.phoneNumber,
			this.email,
			this.manager,
			this.audit.delete(deletedBy)
		);
	}
}
