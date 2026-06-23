package com.bookings.padelcenter.domain.exception;

public class InvalidPasswordException extends RuntimeException {
	public InvalidPasswordException() {
		super("The current password provided is incorrect");
	}
}
