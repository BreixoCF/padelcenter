package com.bookings.padelcenter.domain.exception;

import java.util.UUID;

public class CenterNotFoundException extends ResourceNotFoundException {
	public CenterNotFoundException(UUID centerId) {
		super("Center with ID " + centerId + " not found");
	}
}
