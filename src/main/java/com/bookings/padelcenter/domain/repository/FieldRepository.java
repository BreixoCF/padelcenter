package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.Field;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldRepository {

	List<Field> findAll();
	Field save(Field field);
	Optional<Field> findById(UUID id);
	List<Field> findByCenterId(UUID centerId);
}
