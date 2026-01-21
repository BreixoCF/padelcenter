package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.Center;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CenterRepository {

	List<Center> findAll();
	Center save(Center center);
	Optional<Center> findById(UUID id);
	Optional<Center> findByName(String name);
	boolean existsByName(String name);
}
