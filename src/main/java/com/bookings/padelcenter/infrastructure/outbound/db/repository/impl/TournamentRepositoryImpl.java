package com.bookings.padelcenter.infrastructure.outbound.db.repository.impl;

import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.mapper.TournamentPersistenceMapper;
import com.bookings.padelcenter.infrastructure.outbound.db.repository.TournamentJpaRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.repository.TournamentPairJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TournamentRepositoryImpl implements TournamentRepository {

	private final TournamentJpaRepository tournamentJpaRepository;
	private final TournamentPairJpaRepository pairJpaRepository;
	private final TournamentPersistenceMapper mapper;

	@Override
	public Tournament save(Tournament tournament) {
		var entity = mapper.toEntity(tournament);
		return mapper.toDomain(tournamentJpaRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Tournament> findById(UUID tournamentId) {
		return tournamentJpaRepository.findById(tournamentId).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<Tournament> findByCenterId(UUID centerId, int page, int size) {
		var springPage = tournamentJpaRepository.findByCenterId(centerId, PageRequest.of(page, size));
		var content = springPage.getContent().stream().map(mapper::toDomain).toList();
		return new PageResult<>(content, springPage.getNumber(), springPage.getSize(),
				springPage.getTotalElements(), springPage.getTotalPages());
	}

	@Override
	@Transactional(readOnly = true)
	public List<TournamentPair> findPairsByTournamentId(UUID tournamentId) {
		return pairJpaRepository.findByTournamentId(tournamentId).stream()
				.map(mapper::toPairDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<TournamentPair> findPairById(UUID pairId) {
		return pairJpaRepository.findById(pairId).map(mapper::toPairDomain);
	}

	@Override
	public TournamentPair savePair(TournamentPair pair) {
		var entity = mapper.toPairEntity(pair);
		return mapper.toPairDomain(pairJpaRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsPairWithPlayer(UUID tournamentId, UUID userId) {
		return pairJpaRepository.existsByTournamentIdAndPlayer(tournamentId, userId);
	}

	@Override
	@Transactional
	public void delete(UUID tournamentId, UUID deletedBy) {
		tournamentJpaRepository.findById(tournamentId).ifPresent(entity -> {
			entity.setDeletedBy(deletedBy);
			entity.setDeletedAt(Instant.now());
			entity.setStatus(TournamentStatus.CANCELLED.name());
			tournamentJpaRepository.save(entity);
		});
	}

	@Override
	@Transactional(readOnly = true)
	public long countActivePairs(UUID tournamentId) {
		return pairJpaRepository.countActiveByTournamentId(tournamentId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TournamentPair> findPairsByUserId(UUID userId) {
		return pairJpaRepository.findByPlayer(userId).stream()
				.map(mapper::toPairDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<Tournament> findByPlayerId(UUID userId, int page, int size) {
		var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
		var springPage = tournamentJpaRepository.findByPlayerId(userId, pageable);
		var content = springPage.getContent().stream().map(mapper::toDomain).toList();
		return new PageResult<>(content, springPage.getNumber(), springPage.getSize(),
				springPage.getTotalElements(), springPage.getTotalPages());
	}
}
