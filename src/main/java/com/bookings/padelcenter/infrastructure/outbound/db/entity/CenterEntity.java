package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "centers")
public class CenterEntity extends AuditableEntity<UUID> {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "center_id")
	private UUID centerId;

	private String name;
	private String address;
	private String city;
	private String phoneNumber;
	private String email;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id", referencedColumnName = "user_id")
	private UserEntity manager;
}