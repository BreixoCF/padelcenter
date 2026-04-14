package com.bookings.padelcenter.domain.exception;

public class EmailAlreadyExistsException extends RuntimeException {
	public EmailAlreadyExistsException(String email) {
		super(String.format("El email '%s' ya está registrado por otro usuario", email));
	}
}
