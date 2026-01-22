package com.bookings.padelcenter.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {
	public EmailAlreadyExistsException(String email) {
		super(String.format("El email '%s' ya está registrado por otro usuario", email));
	}
}
