package com.bookings.padelcenter.infrastructure.outbound.db.repository.impl;

import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.FieldEntity;
import com.bookings.padelcenter.infrastructure.outbound.db.mapper.FieldPersistenceMapper;
import com.bookings.padelcenter.infrastructure.outbound.db.repository.FieldJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FieldRepositoryImpl implements FieldRepository {

	private final FieldJpaRepository fieldJpaRepository;
	private final FieldPersistenceMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public List<Field> findAll() {
		return fieldJpaRepository.findAll()
				.stream()
				.map(mapper::toDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public PageResult<Field> findAll(int page, int size) {
		var springPage = fieldJpaRepository.findAll(PageRequest.of(page, size));
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
	public Field save(Field field) {
		FieldEntity entity = mapper.toEntity(field);
		FieldEntity saved = fieldJpaRepository.save(entity);
		return mapper.toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Field> findById(UUID id) {
		return fieldJpaRepository.findById(id)
			.map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Field> findByCenterId(UUID centerId) {
		return fieldJpaRepository.findByCenterCenterId(centerId)
				.stream()
				.map(mapper::toDomain)
				.toList();
	}
}
