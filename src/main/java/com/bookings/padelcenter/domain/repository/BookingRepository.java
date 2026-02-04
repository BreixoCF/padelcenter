package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {

	Booking save(Booking booking);
	Optional<Booking> findById(Long id);
	List<Booking> findAll();
}
