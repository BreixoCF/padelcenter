package com.bookings.padelcenter.infrastructure.outbound.db.repository.impl;

import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.MatchRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.mapper.MatchPersistenceMapper;
import com.bookings.padelcenter.infrastructure.outbound.db.repository.MatchJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
	public PageResult<Match> findByTournamentId(UUID tournamentId, int page, int size) {
		var springPage = matchJpaRepository.findByTournamentIdOrderByRoundAscGroupNameAsc(
				tournamentId, PageRequest.of(page, size));
		var content = springPage.getContent().stream().map(mapper::toDomain).toList();
		return new PageResult<>(content, springPage.getNumber(), springPage.getSize(),
				springPage.getTotalElements(), springPage.getTotalPages());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Match> findByTournamentIdAndRound(UUID tournamentId, int round) {
		return matchJpaRepository.findByTournamentIdAndRound(tournamentId, round)
				.stream().map(mapper::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Match> findByPairIdIn(List<UUID> pairIds) {
		if (pairIds.isEmpty()) return List.of();
		return matchJpaRepository.findByPairIdIn(pairIds)
				.stream().map(mapper::toDomain).toList();
	}
}
