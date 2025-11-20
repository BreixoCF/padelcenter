package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import com.bookings.padelcenter.domain.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "centers")
public class CenterEntity extends Auditable<UUID> {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID centerId;

	private String name;
	private String address;
	private String city;
	private String phoneNumber;
	private String email;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id", referencedColumnName = "userId")
	private UserEntity manager;
}