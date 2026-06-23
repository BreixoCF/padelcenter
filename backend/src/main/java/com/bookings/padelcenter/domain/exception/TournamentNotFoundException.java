package com.bookings.padelcenter.domain.exception;

import java.util.UUID;

public class TournamentNotFoundException extends ResourceNotFoundException {
	public TournamentNotFoundException(UUID tournamentId) {
		super("Tournament with ID " + tournamentId + " not found");
	}
}
