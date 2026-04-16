package com.bookings.padelcenter.domain.repository;

import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.PageResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {

	Booking save(Booking booking);
	Optional<Booking> findById(Long bookingId);
	List<Booking> findAll();
	PageResult<Booking> findByUserId(UUID userId, int page, int size);

	/**
	 * Returns true if any PENDING or CONFIRMED booking for the given field overlaps
	 * the specified time range.
	 *
	 * @param fieldId   field to check
	 * @param start     start of the requested time range (inclusive)
	 * @param end       end of the requested time range (exclusive)
	 * @param excludeId bookingId to exclude from the check (for updates), or null when creating
	 * @return true if an overlapping booking exists
	 */
	boolean existsOverlappingBooking(UUID fieldId, LocalDateTime start, LocalDateTime end, Long excludeId);
}
