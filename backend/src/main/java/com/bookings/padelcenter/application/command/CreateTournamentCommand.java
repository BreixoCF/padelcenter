package com.bookings.padelcenter.application.command;

import com.bookings.padelcenter.domain.model.TournamentFormat;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTournamentCommand(
	UUID centerId,
	String name,
	String description,
	TournamentFormat format,
	int maxPairs,
	LocalDate startDate,
	LocalDate endDate
) {}
