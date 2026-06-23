package com.bookings.padelcenter.domain.exception;

public class InvalidTournamentStatusException extends RuntimeException {
	public InvalidTournamentStatusException(String message) {
		super(message);
	}
}
