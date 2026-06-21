package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tournament_matches")
public class MatchEntity extends AuditableEntity<UUID> {

	@Id
	@Column(name = "match_id")
	private UUID matchId;

	@Column(name = "tournament_id", nullable = false)
	private UUID tournamentId;

	@Column(name = "pair_a_id")
	private UUID pairAId;

	@Column(name = "pair_b_id")
	private UUID pairBId;

	@Column(nullable = false)
	private int round;

	@Column(name = "group_name")
	private String groupName;

	@Column(nullable = false)
	private String status;

	@Column(name = "scheduled_at")
	private Instant scheduledAt;

	@Column(name = "winner_pair_id")
	private UUID winnerPairId;

	@Column(name = "score_a")
	private Integer scoreA;

	@Column(name = "score_b")
	private Integer scoreB;

	@Column(name = "reported_by")
	private UUID reportedBy;

	@Column(name = "reported_at")
	private Instant reportedAt;
}
