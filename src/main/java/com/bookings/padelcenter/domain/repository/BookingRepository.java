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

	/**
	 * Returns all confirmed bookings for the given field that overlap the specified day.
	 *
	 * @param fieldId  target field
	 * @param dayStart start of the day (inclusive)
	 * @param dayEnd   end of the day (exclusive)
	 * @return list of confirmed bookings for that day, ordered by start time
	 */
	List<Booking> findConfirmedByFieldAndDay(UUID fieldId, LocalDateTime dayStart, LocalDateTime dayEnd);

	/**
	 * Returns paginated bookings for all fields of a center,
	 * with optional filters for field, date range, or specific day.
	 * Returns all statuses.
	 *
	 * @param centerId  target center
	 * @param fieldId   optional field filter
	 * @param dayStart  start of range (inclusive), null if no filter
	 * @param dayEnd    end of range (exclusive), null if no filter
	 * @param page      zero-based page number
	 * @param size      page size
	 */
	PageResult<Booking> findByCenterWithFilters(
		UUID centerId,
		UUID fieldId,
		LocalDateTime dayStart,
		LocalDateTime dayEnd,
		int page,
		int size
	);
}
