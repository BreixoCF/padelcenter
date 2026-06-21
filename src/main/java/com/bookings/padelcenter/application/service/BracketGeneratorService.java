package com.bookings.padelcenter.application.service;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.MatchStatus;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentPair;
import org.springframework.stereotype.Service;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Domain service that generates tournament matches based on format and confirmed pairs.
 * Pure domain logic — no repository calls.
 */
@Service
public class BracketGeneratorService {

	/**
	 * Generates all matches for the given tournament and confirmed pairs.
	 *
	 * @param tournament    must be in REGISTRATION_CLOSED status
	 * @param confirmedPairs only CONFIRMED pairs
	 * @return list of matches to persist (matchId is null — assigned by DB on save)
	 */
	public List<Match> generate(Tournament tournament, List<TournamentPair> confirmedPairs) {
		return switch (tournament.format()) {
			case ROUND_ROBIN -> generateRoundRobin(tournament, confirmedPairs);
			case ELIMINATION -> generateElimination(tournament, confirmedPairs);
			case GROUPS_AND_ELIMINATION -> generateGroupsAndElimination(tournament, confirmedPairs);
		};
	}

	// ── Round Robin ─────────────────────────────────────────────────────────

	private List<Match> generateRoundRobin(Tournament t, List<TournamentPair> pairs) {
		var matches = new ArrayList<Match>();
		for (int i = 0; i < pairs.size(); i++) {
			for (int j = i + 1; j < pairs.size(); j++) {
				matches.add(buildMatch(t.tournamentId(), pairs.get(i).pairId(),
						pairs.get(j).pairId(), 1, null));
			}
		}
		return matches;
	}

	// ── Elimination ─────────────────────────────────────────────────────────

	private List<Match> generateElimination(Tournament t, List<TournamentPair> pairs) {
		var shuffled = new ArrayList<>(pairs);
		Collections.shuffle(shuffled);

		var matches = new ArrayList<Match>();
		int n = shuffled.size();

		// Round 1: assign actual pairs
		for (int i = 0; i < n; i += 2) {
			matches.add(buildMatch(t.tournamentId(), shuffled.get(i).pairId(),
					shuffled.get(i + 1).pairId(), 1, null));
		}

		// Future rounds: TBD placeholders (pairAId/pairBId filled when previous round completes)
		int round = 2;
		int matchesInRound = n / 4;
		while (matchesInRound >= 1) {
			for (int i = 0; i < matchesInRound; i++) {
				matches.add(buildMatch(t.tournamentId(), null, null, round, null));
			}
			round++;
			matchesInRound /= 2;
		}
		return matches;
	}

	// ── Groups + Elimination ─────────────────────────────────────────────────

	private List<Match> generateGroupsAndElimination(Tournament t, List<TournamentPair> pairs) {
		var groups = partitionIntoGroups(pairs, 4);
		var matches = new ArrayList<Match>();

		// Group phase: full round robin within each group
		for (int g = 0; g < groups.size(); g++) {
			var group = groups.get(g);
			var groupName = String.valueOf((char) ('A' + g));
			for (int i = 0; i < group.size(); i++) {
				for (int j = i + 1; j < group.size(); j++) {
					matches.add(buildMatch(t.tournamentId(), group.get(i).pairId(),
							group.get(j).pairId(), 1, groupName));
				}
			}
		}

		// Elimination phase: 2 qualifiers per group, TBD placeholders
		int qualifiers = groups.size() * 2;
		int elimRound = 2;
		int elimMatches = qualifiers / 2;
		while (elimMatches >= 1) {
			for (int i = 0; i < elimMatches; i++) {
				matches.add(buildMatch(t.tournamentId(), null, null, elimRound, null));
			}
			elimRound++;
			elimMatches /= 2;
		}

		return matches;
	}

	// ── Helpers ──────────────────────────────────────────────────────────────

	private Match buildMatch(UUID tournamentId, UUID pairAId, UUID pairBId,
	                          int round, String groupName) {
		return new Match(UuidCreator.getTimeOrderedEpoch(), tournamentId, pairAId, pairBId, round, groupName,
				MatchStatus.SCHEDULED, null, null, Auditable.newAudit());
	}

	private <T> List<List<T>> partitionIntoGroups(List<T> items, int groupSize) {
		var groups = new ArrayList<List<T>>();
		for (int i = 0; i < items.size(); i += groupSize) {
			groups.add(new ArrayList<>(items.subList(i, Math.min(i + groupSize, items.size()))));
		}
		return groups;
	}

	public boolean isPowerOfTwo(int n) {
		return n > 0 && (n & (n - 1)) == 0;
	}
}
