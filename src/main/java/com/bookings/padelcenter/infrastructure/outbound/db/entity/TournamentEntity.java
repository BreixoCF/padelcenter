package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tournaments")
public class TournamentEntity extends AuditableEntity<UUID> {

	@Id
	@Column(name = "tournament_id")
	private UUID tournamentId;

	@Column(name = "center_id", nullable = false)
	private UUID centerId;

	@Column(nullable = false)
	private String name;

	private String description;

	@Column(nullable = false)
	private String format;

	@Column(nullable = false)
	private String status;

	@Column(name = "max_pairs", nullable = false)
	private int maxPairs;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;
}
