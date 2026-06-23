package com.bookings.padelcenter.domain.exception;

import java.util.UUID;

public class PlayerAlreadyInPairException extends RuntimeException {
	public PlayerAlreadyInPairException(UUID userId) {
		super("Player " + userId + " is already registered in another pair for this tournament");
	}
}
