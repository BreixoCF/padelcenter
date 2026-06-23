package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Driven port for Match persistence.
 */
public interface MatchRepository {

	Match save(Match match);

	void saveAll(List<Match> matches);

	Optional<Match> findById(UUID matchId);

	List<Match> findByTournamentId(UUID tournamentId);

	PageResult<Match> findByTournamentId(UUID tournamentId, int page, int size);

	List<Match> findByTournamentIdAndRound(UUID tournamentId, int round);

	List<Match> findByPairIdIn(List<UUID> pairIds);
}
