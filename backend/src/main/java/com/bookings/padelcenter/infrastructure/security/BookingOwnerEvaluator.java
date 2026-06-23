package com.bookings.padelcenter.infrastructure.security;

import com.bookings.padelcenter.domain.exception.BookingNotFoundException;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * SpEL-accessible evaluator that checks whether the authenticated user
 * owns a given booking. Used in {@code @PreAuthorize} expressions.
 *
 * @see com.bookings.padelcenter.infrastructure.inbound.web.BookingController
 */
@Component("bookingOwnerEvaluator")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingOwnerEvaluator {

	private final BookingRepository bookingRepository;

	public boolean isOwner(UUID userId, Long bookingId) {
		return bookingRepository.findById(bookingId)
				.map(booking -> booking.user().userId().equals(userId))
				.orElseThrow(() -> new BookingNotFoundException(bookingId));
	}
}
