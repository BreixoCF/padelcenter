package com.bookings.padelcenter.infrastructure.outbound.db.repository.impl;

import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.CenterEntity;
import com.bookings.padelcenter.infrastructure.outbound.db.mapper.CenterPersistenceMapper;
import com.bookings.padelcenter.infrastructure.outbound.db.repository.CenterJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CenterRepositoryImpl implements CenterRepository {

	private final CenterJpaRepository centerJpaRepository;
	private final CenterPersistenceMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public List<Center> findAll() {
		return centerJpaRepository.findAll()
				.stream()
				.map(mapper::toDomain)
				.toList();
	}

	@Override
	public Center save(Center center) {
		CenterEntity entity = mapper.toEntity(center);
		CenterEntity saved = centerJpaRepository.save(entity);
		return mapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Center> findById(UUID id) {
		return centerJpaRepository.findById(id)
				.map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Center> findByName(String name) {
		return centerJpaRepository.findByName(name)
				.map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByName(String name) {
		return centerJpaRepository.existsByName(name);
	}
}
