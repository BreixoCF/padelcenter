package com.bookings.padelcenter.domain.model;

import java.util.UUID;

public record CenterRole(
	UUID centerId,
	Role role,
	Auditable audit
) {}
