package com.bookings.padelcenter.domain.exception;

import java.util.UUID;

public class MatchNotFoundException extends ResourceNotFoundException {
	public MatchNotFoundException(UUID matchId) {
		super("Match with ID " + matchId + " not found");
	}
}
