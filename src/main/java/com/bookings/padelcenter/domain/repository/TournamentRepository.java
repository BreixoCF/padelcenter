package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentPair;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Driven port for Tournament and TournamentPair persistence.
 */
public interface TournamentRepository {

	Tournament save(Tournament tournament);

	Optional<Tournament> findById(UUID tournamentId);

	PageResult<Tournament> findByCenterId(UUID centerId, int page, int size);

	List<TournamentPair> findPairsByTournamentId(UUID tournamentId);

	Optional<TournamentPair> findPairById(UUID pairId);

	TournamentPair savePair(TournamentPair pair);

	/**
	 * Returns true if the given user is already registered as player1 or player2
	 * in any pair (regardless of status) for the specified tournament.
	 */
	boolean existsPairWithPlayer(UUID tournamentId, UUID userId);

	/**
	 * Returns tournaments where the given user is registered in a CONFIRMED pair.
	 */
	PageResult<Tournament> findByPlayerId(UUID userId, int page, int size);
}
