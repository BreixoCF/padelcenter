package com.bookings.padelcenter.application.command;

import java.util.UUID;

public record RegisterPairCommand(
	UUID tournamentId,
	UUID player1Id,
	UUID player2Id,
	String teamName
) {}
