package com.bookings.padelcenter.infrastructure.inbound.dto.request;

import com.bookings.padelcenter.domain.model.Role;

import java.util.UUID;

public record CenterRoleRequest(
		UUID centerId,
		Role role
) {}
