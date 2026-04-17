package com.bookings.padelcenter.application.query;

import java.time.LocalDate;
import java.util.UUID;

public record GetCenterBookingsQuery(
	UUID centerId,
	UUID fieldId,
	LocalDate date,
	LocalDate startDate,
	LocalDate endDate,
	int page,
	int size
) {}
