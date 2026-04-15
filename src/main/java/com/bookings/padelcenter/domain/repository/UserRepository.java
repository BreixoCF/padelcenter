package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

	PageResult<User> findAll(int page, int size);
	User save(User user);
	Optional<User> findById(UUID id);
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
}
