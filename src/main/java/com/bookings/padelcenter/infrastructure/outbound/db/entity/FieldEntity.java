package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import com.bookings.padelcenter.domain.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "fields")
public class FieldEntity extends Auditable<UUID> {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "center_id", referencedColumnName = "centerId", nullable = false)
	private CenterEntity center;

	private String number;
	private String type;

	@Column(precision = 10, scale = 2)
	private BigDecimal pricePerHour;

	@Column(name = "is_available")
	private Boolean isAvailable;
}