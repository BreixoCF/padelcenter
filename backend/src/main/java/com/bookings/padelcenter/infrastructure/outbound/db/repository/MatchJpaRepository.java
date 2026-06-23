package com.bookings.padelcenter.infrastructure.outbound.db.repository;

import com.bookings.padelcenter.infrastructure.outbound.db.entity.MatchEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MatchJpaRepository extends JpaRepository<MatchEntity, UUID> {

	List<MatchEntity> findByTournamentIdOrderByRoundAscGroupNameAsc(UUID tournamentId);

	Page<MatchEntity> findByTournamentIdOrderByRoundAscGroupNameAsc(UUID tournamentId, Pageable pageable);

	List<MatchEntity> findByTournamentIdAndRound(UUID tournamentId, int round);

	@Query("""
		SELECT m FROM MatchEntity m
		WHERE m.pairAId IN :pairIds OR m.pairBId IN :pairIds
		ORDER BY m.scheduledAt DESC
		""")
	List<MatchEntity> findByPairIdIn(@Param("pairIds") List<UUID> pairIds);
}
