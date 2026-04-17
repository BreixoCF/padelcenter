package com.bookings.padelcenter.domain.model;

import java.time.Instant;
import java.util.UUID;

public record TournamentPair(
	UUID pairId,
	UUID tournamentId,
	UUID player1Id,
	UUID player2Id,
	PairStatus status,
	Instant registeredAt
) {
	public TournamentPair confirm() {
		return new TournamentPair(pairId, tournamentId, player1Id, player2Id,
				PairStatus.CONFIRMED, registeredAt);
	}

	public TournamentPair reject() {
		return new TournamentPair(pairId, tournamentId, player1Id, player2Id,
				PairStatus.REJECTED, registeredAt);
	}
}
