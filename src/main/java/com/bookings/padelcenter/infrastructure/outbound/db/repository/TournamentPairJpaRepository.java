package com.bookings.padelcenter.infrastructure.outbound.db.repository;

import com.bookings.padelcenter.infrastructure.outbound.db.entity.TournamentPairEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TournamentPairJpaRepository extends JpaRepository<TournamentPairEntity, UUID> {

	List<TournamentPairEntity> findByTournamentId(UUID tournamentId);

	@Query("""
		SELECT COUNT(p) > 0 FROM TournamentPairEntity p
		WHERE p.tournamentId = :tournamentId
		  AND (p.player1Id = :userId OR p.player2Id = :userId)
		""")
	boolean existsByTournamentIdAndPlayer(
		@Param("tournamentId") UUID tournamentId,
		@Param("userId") UUID userId
	);
}
