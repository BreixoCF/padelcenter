package com.bookings.padelcenter.infrastructure.outbound.db.repository.impl;

import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.UserRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.repository.UserJpaRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.mapper.UserPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;
	private final UserPersistenceMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public PageResult<User> findAll(int page, int size) {
		var springPage = userJpaRepository.findAll(PageRequest.of(page, size));
		var content = springPage.getContent()
				.stream()
				.map(mapper::toDomain)
				.toList();
		return new PageResult<>(
				content,
				springPage.getNumber(),
				springPage.getSize(),
				springPage.getTotalElements(),
				springPage.getTotalPages()
		);
	}

	@Override
	public User save(User user) {
		var entity = mapper.toEntity(user);
		var saved = userJpaRepository.save(entity);
		return mapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findById(UUID id) {
		return userJpaRepository.findById(id).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findByEmail(String email) {
		return userJpaRepository.findByEmail(email).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findByKeycloakId(String keycloakId) {
		return userJpaRepository.findByKeycloakId(keycloakId).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userJpaRepository.existsByEmail(email);
	}
}
