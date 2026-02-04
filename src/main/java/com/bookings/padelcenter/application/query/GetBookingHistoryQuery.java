package com.bookings.padelcenter.application.query;

import java.util.UUID;

public record GetBookingHistoryQuery(
	UUID userId
) {}
