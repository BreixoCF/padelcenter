package com.bookings.padelcenter.infrastructure.outbound.db.repository.impl;

import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.UserEntity;
import com.bookings.padelcenter.infrastructure.outbound.db.repository.UserJpaRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;
	private final UserPersistenceMapper mapper;

	@Override
	public List<User> findAll() {
		var entities = userJpaRepository.findAll();
		return entities.stream().map(mapper::toDomain).toList();
	}

	@Override
	public User save(User user) {
		UserEntity entity = mapper.toEntity(user);
		UserEntity saved = userJpaRepository.save(entity);
		return mapper.toDomain(saved);
	}

	@Override
	public Optional<User> findById(UUID id) {
		return userJpaRepository.findById(id).map(mapper::toDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userJpaRepository.findByEmail(email).map(mapper::toDomain);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userJpaRepository.existsByEmail(email);
	}

}