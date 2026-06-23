package com.bookings.padelcenter.application.service;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.PairStatus;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class BracketGeneratorServiceTest {

	private BracketGeneratorService service;

	@BeforeEach
	void setUp() {
		service = new BracketGeneratorService();
	}

	// ── Round Robin ──────────────────────────────────────────────────────────

	@Test
	@DisplayName("should generate 6 matches for 4 pairs in round robin")
	void generate_4pairs_roundRobin_generates6Matches() {
		var tournament = buildTournament(TournamentFormat.ROUND_ROBIN);
		var pairs = buildPairs(4, tournament.tournamentId());

		var matches = service.generate(tournament, pairs);

		assertThat(matches).hasSize(6); // 4*(4-1)/2 = 6
	}

	@Test
	@DisplayName("should generate 15 matches for 6 pairs in round robin")
	void generate_6pairs_roundRobin_generates15Matches() {
		var tournament = buildTournament(TournamentFormat.ROUND_ROBIN);
		var pairs = buildPairs(6, tournament.tournamentId());

		var matches = service.generate(tournament, pairs);

		assertThat(matches).hasSize(15); // 6*(6-1)/2 = 15
	}

	@Test
	@DisplayName("every pair should play against every other pair exactly once in round robin")
	void generate_roundRobin_allPairsPlayEachOther() {
		var tournament = buildTournament(TournamentFormat.ROUND_ROBIN);
		var pairs = buildPairs(4, tournament.tournamentId());

		var matches = service.generate(tournament, pairs);

		// Each pair should appear in exactly 3 matches (n-1 = 3)
		for (var pair : pairs) {
			long count = matches.stream()
					.filter(m -> pair.pairId().equals(m.pairAId()) || pair.pairId().equals(m.pairBId()))
					.count();
			assertThat(count).isEqualTo(3);
		}
	}

	// ── Elimination ──────────────────────────────────────────────────────────

	@Test
	@DisplayName("should generate 3 matches for 4 pairs in elimination")
	void generate_4pairs_elimination_generates3Matches() {
		var tournament = buildTournament(TournamentFormat.ELIMINATION);
		var pairs = buildPairs(4, tournament.tournamentId());

		var matches = service.generate(tournament, pairs);

		assertThat(matches).hasSize(3); // n-1 = 3
	}

	@Test
	@DisplayName("should generate 7 matches for 8 pairs in elimination")
	void generate_8pairs_elimination_generates7Matches() {
		var tournament = buildTournament(TournamentFormat.ELIMINATION);
		var pairs = buildPairs(8, tournament.tournamentId());

		var matches = service.generate(tournament, pairs);

		assertThat(matches).hasSize(7); // n-1 = 7
	}

	@Test
	@DisplayName("round 1 should have real pairs, later rounds should be TBD in elimination")
	void generate_elimination_round1HasPairs_laterRoundsAreTBD() {
		var tournament = buildTournament(TournamentFormat.ELIMINATION);
		var pairs = buildPairs(8, tournament.tournamentId());

		var matches = service.generate(tournament, pairs);

		var round1 = matches.stream().filter(m -> m.round() == 1).toList();
		var round2 = matches.stream().filter(m -> m.round() == 2).toList();

		assertThat(round1).hasSize(4);
		assertThat(round1).allSatisfy(m -> {
			assertThat(m.pairAId()).isNotNull();
			assertThat(m.pairBId()).isNotNull();
		});

		assertThat(round2).hasSize(2);
		assertThat(round2).allSatisfy(m -> {
			assertThat(m.pairAId()).isNull();
			assertThat(m.pairBId()).isNull();
		});
	}

	// ── Groups + Elimination ─────────────────────────────────────────────────

	@Test
	@DisplayName("should generate 15 matches for 8 pairs in groups+elimination (12 group + 3 elimination)")
	void generate_8pairs_groupsElimination_generates15Matches() {
		var tournament = buildTournament(TournamentFormat.GROUPS_AND_ELIMINATION);
		var pairs = buildPairs(8, tournament.tournamentId());

		var matches = service.generate(tournament, pairs);

		// 2 groups of 4 → 2 * 6 = 12 group matches
		// 2 groups * 2 qualifiers = 4 qualifiers → 3 elimination matches
		assertThat(matches).hasSize(15);
	}

	@Test
	@DisplayName("group matches should have group names A and B for 8 pairs")
	void generate_groupsElimination_createsGroupsWithNames() {
		var tournament = buildTournament(TournamentFormat.GROUPS_AND_ELIMINATION);
		var pairs = buildPairs(8, tournament.tournamentId());

		var matches = service.generate(tournament, pairs);

		var groupAMatches = matches.stream().filter(m -> "A".equals(m.groupName())).toList();
		var groupBMatches = matches.stream().filter(m -> "B".equals(m.groupName())).toList();

		assertThat(groupAMatches).hasSize(6); // 4 pairs → 6 round-robin matches
		assertThat(groupBMatches).hasSize(6);
	}

	// ── Helpers ──────────────────────────────────────────────────────────────

	private Tournament buildTournament(TournamentFormat format) {
		return new Tournament(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"Test Tournament",
				null,
				format,
				TournamentStatus.REGISTRATION_CLOSED,
				16,
				LocalDate.now(),
				LocalDate.now().plusDays(7),
				Auditable.newAudit()
		);
	}

	private List<TournamentPair> buildPairs(int n, UUID tournamentId) {
		return IntStream.range(0, n)
				.mapToObj(i -> new TournamentPair(
						UUID.randomUUID(),
						tournamentId,
						UUID.randomUUID(),
						UUID.randomUUID(),
						"",
						PairStatus.CONFIRMED,
						Instant.now()
				))
				.toList();
	}
}
