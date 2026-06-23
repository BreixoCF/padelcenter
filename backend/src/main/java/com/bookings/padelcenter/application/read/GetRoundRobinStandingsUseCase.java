package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetRoundRobinStandingsQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.InvalidTournamentStatusException;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.MatchStatus;
import com.bookings.padelcenter.domain.model.RoundRobinStanding;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.repository.MatchRepository;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetRoundRobinStandingsUseCase implements QueryUseCase<GetRoundRobinStandingsQuery, List<RoundRobinStanding>> {

	private final TournamentRepository tournamentRepository;
	private final MatchRepository matchRepository;

	@NonNull
	@Override
	public List<RoundRobinStanding> execute(GetRoundRobinStandingsQuery query) {
		var tournament = tournamentRepository.findById(query.tournamentId())
				.orElseThrow(() -> new TournamentNotFoundException(query.tournamentId()));

		if (tournament.format() != TournamentFormat.ROUND_ROBIN) {
			throw new InvalidTournamentStatusException(
					"Tournament " + query.tournamentId() + " is not ROUND_ROBIN format");
		}

		var pairs = tournamentRepository.findPairsByTournamentId(query.tournamentId());
		var matches = matchRepository.findByTournamentId(query.tournamentId());

		Map<UUID, String> teamNames = new HashMap<>();
		for (var pair : pairs) {
			teamNames.put(pair.pairId(), pair.teamName());
		}

		Map<UUID, int[]> stats = new HashMap<>();
		for (var pair : pairs) {
			stats.put(pair.pairId(), new int[6]); // played, wins, losses, setsFor, setsAgainst, unused
		}

		for (var match : matches) {
			if (match.status() != MatchStatus.COMPLETED || match.result() == null) continue;
			var result = match.result();
			var pairA = match.pairAId();
			var pairB = match.pairBId();

			if (!stats.containsKey(pairA) || !stats.containsKey(pairB)) continue;

			var stA = stats.get(pairA);
			var stB = stats.get(pairB);

			stA[0]++; stB[0]++; // played
			stA[3] += result.scoreA(); stA[4] += result.scoreB();
			stB[3] += result.scoreB(); stB[4] += result.scoreA();

			if (result.winnerPairId().equals(pairA)) {
				stA[1]++; stB[2]++;
			} else {
				stB[1]++; stA[2]++;
			}
		}

		List<RoundRobinStanding> standings = new ArrayList<>();
		for (var entry : stats.entrySet()) {
			var s = entry.getValue();
			standings.add(new RoundRobinStanding(
					entry.getKey(),
					teamNames.getOrDefault(entry.getKey(), ""),
					s[0], s[1], s[2], s[3], s[4], s[3] - s[4]
			));
		}

		standings.sort((a, b) -> {
			int winsComp = Integer.compare(b.wins(), a.wins());
			if (winsComp != 0) return winsComp;
			return Integer.compare(b.setDifference(), a.setDifference());
		});

		return standings;
	}
}
