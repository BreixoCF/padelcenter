package com.bookings.padelcenter.application.query;

import java.util.UUID;

public record GetTournamentMatchesQuery(UUID tournamentId, int page, int size) {}
