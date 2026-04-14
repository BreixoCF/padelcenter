package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.Field;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FieldRepository {

	List<Field> findAll();
	Page<Field> findAll(Pageable pageable);
	Field save(Field field);
	Optional<Field> findById(UUID id);
	List<Field> findByCenterId(UUID centerId);
}
