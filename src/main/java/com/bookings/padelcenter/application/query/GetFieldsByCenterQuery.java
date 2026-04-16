package com.bookings.padelcenter.application.query;

import java.util.UUID;

public record GetFieldsByCenterQuery(
	UUID centerId,
	String type,
	Boolean available,
	int page,
	int size
) {}
