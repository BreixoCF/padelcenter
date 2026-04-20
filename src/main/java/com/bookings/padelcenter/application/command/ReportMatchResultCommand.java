package com.bookings.padelcenter.application.command;

import java.util.UUID;

public record ReportMatchResultCommand(
	UUID matchId,
	UUID winnerPairId,
	int scoreA,
	int scoreB,
	UUID reportedBy,
	boolean isAdmin
) {}
