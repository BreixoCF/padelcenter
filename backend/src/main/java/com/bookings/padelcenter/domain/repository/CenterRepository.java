package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.model.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface CenterRepository {

	PageResult<Center> findAll(int page, int size);
	Center save(Center center);
	Optional<Center> findById(UUID centerId);
	Optional<Center> findByName(String name);
	boolean existsByName(String name);
}
