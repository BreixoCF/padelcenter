package com.bookings.padelcenter.domain.model;

import java.util.Set;
import java.util.UUID;

public record User(
	UUID id,
	String firstName,
	String lastName,
	String email,
	String passwordHash,
	String phoneNumber,
	Set<CenterRole> centerRoles,
	Auditable audit
) {
	public Role getRoleInCenter(UUID centerId) {
		return centerRoles.stream()
			.filter(cr -> cr.centerId().equals(centerId))
			.map(CenterRole::role)
			.findFirst()
			.orElse(Role.USER);
	}
}
