package com.bookings.padelcenter.infrastructure.outbound.db.repository;

import com.bookings.padelcenter.infrastructure.outbound.db.entity.TournamentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TournamentJpaRepository extends JpaRepository<TournamentEntity, UUID> {

	Page<TournamentEntity> findByCenterId(UUID centerId, Pageable pageable);

	@Query(value = """
		SELECT t FROM TournamentEntity t
		WHERE t.tournamentId IN (
		    SELECT p.tournamentId FROM TournamentPairEntity p
		    WHERE (p.player1Id = :userId OR p.player2Id = :userId)
		      AND p.status = 'CONFIRMED'
		)
		""",
		countQuery = """
		SELECT COUNT(DISTINCT t) FROM TournamentEntity t
		WHERE t.tournamentId IN (
		    SELECT p.tournamentId FROM TournamentPairEntity p
		    WHERE (p.player1Id = :userId OR p.player2Id = :userId)
		      AND p.status = 'CONFIRMED'
		)
		""")
	Page<TournamentEntity> findByPlayerId(@Param("userId") UUID userId, Pageable pageable);
}
