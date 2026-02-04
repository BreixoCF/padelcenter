package com.bookings.padelcenter.domain.exception;

import java.util.UUID;

public class FieldNotFoundException extends ResourceNotFoundException {
	public FieldNotFoundException(UUID fieldId) {
		super("Field with ID " + fieldId + " not found");
	}
}
