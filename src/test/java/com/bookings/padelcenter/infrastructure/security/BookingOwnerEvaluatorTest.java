package com.bookings.padelcenter.infrastructure.security;

import com.bookings.padelcenter.domain.exception.BookingNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.BookingStatus;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingOwnerEvaluator")
class BookingOwnerEvaluatorTest {

	@Mock BookingRepository bookingRepository;
	@InjectMocks BookingOwnerEvaluator evaluator;

	private static final UUID OWNER_ID = UUID.randomUUID();
	private static final UUID OTHER_USER_ID = UUID.randomUUID();
	private static final Long BOOKING_ID = 1L;

	@Test
	@DisplayName("should return true when userId matches booking owner")
	void isOwner_matchingUser_returnsTrue() {
		// given
		given(bookingRepository.findById(BOOKING_ID))
				.willReturn(Optional.of(bookingWithOwner(OWNER_ID)));

		// when
		var result = evaluator.isOwner(OWNER_ID, BOOKING_ID);

		// then
		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("should return false when userId does not match booking owner")
	void isOwner_differentUser_returnsFalse() {
		// given
		given(bookingRepository.findById(BOOKING_ID))
				.willReturn(Optional.of(bookingWithOwner(OWNER_ID)));

		// when
		var result = evaluator.isOwner(OTHER_USER_ID, BOOKING_ID);

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("should throw BookingNotFoundException when booking does not exist")
	void isOwner_bookingNotFound_throws() {
		// given
		given(bookingRepository.findById(BOOKING_ID))
				.willReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> evaluator.isOwner(OWNER_ID, BOOKING_ID))
				.isInstanceOf(BookingNotFoundException.class);
	}

	private static Booking bookingWithOwner(UUID userId) {
		var audit = new Auditable(userId, Instant.now(), null, null, null, null);
		var user = new User(userId, null, "Test", "User", "test@example.com",
				"hash", "600000001", Set.of(), audit);
		return new Booking(BOOKING_ID, user, null,
				LocalDateTime.of(2025, 6, 1, 10, 0),
				LocalDateTime.of(2025, 6, 1, 11, 0),
				BigDecimal.valueOf(25), Instant.now(), BookingStatus.PENDING, audit);
	}
}
