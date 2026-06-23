package com.bookings.padelcenter.domain.model;

import java.time.Instant;
import java.util.UUID;

public record MatchResult(
	UUID winnerPairId,
	int scoreA,
	int scoreB,
	UUID reportedBy,
	Instant reportedAt
) {}
