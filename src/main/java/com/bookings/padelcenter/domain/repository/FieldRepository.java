package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.Field;

import java.util.List;
import java.util.UUID;

public interface FieldRepository {

	List<Field> findAll();
	Field save(Field field);
	List<Field> findByCenterId(UUID centerId);
}
