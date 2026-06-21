package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "fields")
public class FieldEntity extends AuditableEntity<UUID> {

	@Id
	@Column(name = "field_id")
	private UUID fieldId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "center_id", referencedColumnName = "center_id", nullable = false)
	private CenterEntity center;

	private String name;
	private String type;

	@Column(precision = 10, scale = 2)
	private BigDecimal pricePerHour;

	@Column(name = "is_available")
	private Boolean isAvailable;
}