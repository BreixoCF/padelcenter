package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {

	Booking save(Booking booking);
	Optional<Booking> findById(Long bookingId);
	List<Booking> findAll();
	PageResult<Booking> findByUserId(UUID userId, int page, int size);
}
