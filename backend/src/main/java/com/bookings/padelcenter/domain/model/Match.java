package com.bookings.padelcenter.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Match(
	UUID matchId,
	UUID tournamentId,
	UUID pairAId,
	UUID pairBId,
	int round,
	String groupName,
	MatchStatus status,
	LocalDateTime scheduledAt,
	MatchResult result,
	Auditable audit
) {
	/**
	 * Returns true if the given user is a player in either pair of this match,
	 * and therefore is allowed to report a result.
	 */
	public boolean isReportableBy(UUID userId, TournamentPair pairA, TournamentPair pairB) {
		return pairA.player1Id().equals(userId) ||
			   pairA.player2Id().equals(userId) ||
			   pairB.player1Id().equals(userId) ||
			   pairB.player2Id().equals(userId);
	}

	public Match withResult(MatchResult matchResult) {
		return new Match(matchId, tournamentId, pairAId, pairBId, round, groupName,
				MatchStatus.COMPLETED, scheduledAt, matchResult, audit);
	}
}
