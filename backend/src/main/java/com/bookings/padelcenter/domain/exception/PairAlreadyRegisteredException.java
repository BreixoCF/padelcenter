package com.bookings.padelcenter.domain.exception;

import java.util.UUID;

public class PairAlreadyRegisteredException extends RuntimeException {
	public PairAlreadyRegisteredException(UUID tournamentId, UUID userId) {
		super("Player " + userId + " is already registered in a pair for tournament " + tournamentId);
	}
}
