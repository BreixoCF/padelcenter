package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "center_roles")
public class CenterRoleEntity extends Auditable<UUID> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long centerRoleId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_name", nullable = false)
	private Role role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "center_id", nullable = false)
	private CenterEntity center;

}
