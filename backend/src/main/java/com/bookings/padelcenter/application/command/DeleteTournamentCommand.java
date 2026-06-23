package com.bookings.padelcenter.application.command;

import java.util.UUID;

public record DeleteTournamentCommand(UUID tournamentId, UUID deletedBy) {}
