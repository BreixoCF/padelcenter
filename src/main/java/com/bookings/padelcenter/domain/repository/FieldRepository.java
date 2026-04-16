package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.model.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FieldRepository {

	List<Field> findAll();
	PageResult<Field> findAll(int page, int size);
	Field save(Field field);
	Optional<Field> findById(UUID fieldId);
	List<Field> findByCenterId(UUID centerId);

	/**
	 * Returns a paginated list of fields for the given center,
	 * optionally filtered by type and availability.
	 *
	 * @param centerId  target center
	 * @param type      optional field type filter
	 * @param available optional availability filter
	 * @param page      zero-based page number
	 * @param size      page size
	 * @return paginated result
	 */
	PageResult<Field> findByCenterWithFilters(UUID centerId, String type, Boolean available, int page, int size);
}
