package com.bookings.padelcenter.domain.exception;

import java.util.UUID;

public class FieldNotAvailableException extends RuntimeException {
	public FieldNotAvailableException(UUID fieldId) {
		super("Field " + fieldId + " is not available for booking");
	}
}
