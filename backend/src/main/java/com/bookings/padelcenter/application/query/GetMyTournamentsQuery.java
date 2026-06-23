package com.bookings.padelcenter.application.query;

import java.util.UUID;

public record GetMyTournamentsQuery(UUID userId, int page, int size) {}
