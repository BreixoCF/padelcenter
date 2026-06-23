package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
public class UserEntity extends AuditableEntity<UUID> {

	@Id
	@Column(name = "user_id")
	private UUID userId;
	private String firstName;
	private String lastName;
	private String email;
	private String passwordHash;
	private String phoneNumber;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<CenterRoleEntity> centerRoles = new HashSet<>();
}