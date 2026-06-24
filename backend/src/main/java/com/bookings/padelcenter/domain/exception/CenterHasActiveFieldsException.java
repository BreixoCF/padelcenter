package com.bookings.padelcenter.domain.exception;

import java.util.UUID;

public class CenterHasActiveFieldsException extends RuntimeException {
	public CenterHasActiveFieldsException(UUID centerId) {
		super("Center with ID " + centerId + " still has active fields and cannot be deleted");
	}
}
