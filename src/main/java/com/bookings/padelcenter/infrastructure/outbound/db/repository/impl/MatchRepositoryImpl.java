package com.bookings.padelcenter.infrastructure.outbound.db.repository.impl;

import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.repository.MatchRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.mapper.MatchPersistenceMapper;
import com.bookings.padelcenter.infrastructure.outbound.db.repository.MatchJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MatchRepositoryImpl implements MatchRepository {

	private final MatchJpaRepository matchJpaRepository;
	private final MatchPersistenceMapper mapper;

	@Override
	public Match save(Match match) {
		return mapper.toDomain(matchJpaRepository.save(mapper.toEntity(match)));
	}

	@Override
	public void saveAll(List<Match> matches) {
		matchJpaRepository.saveAll(matches.stream().map(mapper::toEntity).toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Match> findById(UUID matchId) {
		return matchJpaRepository.findById(matchId).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Match> findByTournamentId(UUID tournamentId) {
		return matchJpaRepository.findByTournamentIdOrderByRoundAscGroupNameAsc(tournamentId)
				.stream().map(mapper::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Match> findByTournamentIdAndRound(UUID tournamentId, int round) {
		return matchJpaRepository.findByTournamentIdAndRound(tournamentId, round)
				.stream().map(mapper::toDomain).toList();
	}
}
