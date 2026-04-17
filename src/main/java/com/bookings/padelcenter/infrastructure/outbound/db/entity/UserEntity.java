package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity extends AuditableEntity<UUID> {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "user_id")
	private UUID userId;
	@Column(name = "keycloak_id", unique = true)
	private String keycloakId;
	private String firstName;
	private String lastName;
	private String email;
	private String passwordHash;
	private String phoneNumber;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<CenterRoleEntity> centerRoles = new HashSet<>();
}