package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tournament_pairs")
public class TournamentPairEntity {

	@Id
	@Column(name = "pair_id")
	private UUID pairId;

	@Column(name = "tournament_id", nullable = false)
	private UUID tournamentId;

	@Column(name = "player1_id", nullable = false)
	private UUID player1Id;

	@Column(name = "player2_id", nullable = false)
	private UUID player2Id;

	@Column(name = "team_name", nullable = false)
	private String teamName;

	@Column(nullable = false)
	private String status;

	@Column(name = "registered_at", nullable = false)
	private Instant registeredAt;
}
