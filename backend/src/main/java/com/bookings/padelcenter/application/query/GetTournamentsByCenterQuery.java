package com.bookings.padelcenter.application.query;

import java.util.UUID;

public record GetTournamentsByCenterQuery(UUID centerId, int page, int size) {}
