package com.bookings.padelcenter.application.query;

import java.util.UUID;

public record GetMyMatchesQuery(UUID userId, int page, int size) {}
