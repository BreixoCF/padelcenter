package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

	List<User> findAll();
	User save(User user);
	Optional<User> findById(UUID id);
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
}
