package com.bookings.padelcenter.application.query;

import java.time.LocalDate;
import java.util.UUID;

public record GetFieldAvailabilityQuery(
	UUID fieldId,
	LocalDate date
) {}
