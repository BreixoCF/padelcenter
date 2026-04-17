package com.bookings.padelcenter.domain.model;

import com.bookings.padelcenter.domain.exception.InvalidTournamentStatusException;

import java.time.LocalDate;
import java.util.UUID;

public record Tournament(
	UUID tournamentId,
	UUID centerId,
	String name,
	String description,
	TournamentFormat format,
	TournamentStatus status,
	int maxPairs,
	LocalDate startDate,
	LocalDate endDate,
	Auditable audit
) {
	public boolean canOpenRegistration() {
		return status == TournamentStatus.DRAFT;
	}

	public boolean canCloseRegistration() {
		return status == TournamentStatus.REGISTRATION_OPEN;
	}

	public boolean canGenerateBracket() {
		return status == TournamentStatus.REGISTRATION_CLOSED;
	}

	public Tournament openRegistration() {
		if (!canOpenRegistration()) {
			throw new InvalidTournamentStatusException(
					"Cannot open registration from status: " + status);
		}
		return withStatus(TournamentStatus.REGISTRATION_OPEN);
	}

	public Tournament closeRegistration() {
		if (!canCloseRegistration()) {
			throw new InvalidTournamentStatusException(
					"Cannot close registration from status: " + status);
		}
		return withStatus(TournamentStatus.REGISTRATION_CLOSED);
	}

	public Tournament startTournament() {
		return withStatus(TournamentStatus.IN_PROGRESS);
	}

	private Tournament withStatus(TournamentStatus newStatus) {
		return new Tournament(tournamentId, centerId, name, description,
				format, newStatus, maxPairs, startDate, endDate, audit);
	}
}
