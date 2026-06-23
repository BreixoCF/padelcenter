package com.bookings.padelcenter.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Field(
	UUID fieldId,
	String name,
	String type,
	BigDecimal pricePerHour,
	Boolean isAvailable,
	Center center,
	Auditable audit
) {
	public Field delete(UUID deletedBy) {
		return new Field(
			this.fieldId,
			this.name,
			this.type,
			this.pricePerHour,
			this.isAvailable,
			this.center,
			this.audit.delete(deletedBy)
		);
	}
}
