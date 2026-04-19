package com.bookings.padelcenter.application.query;

import java.util.UUID;

public record GetMyBookingsQuery(UUID userId, String status, int page, int size) {}
