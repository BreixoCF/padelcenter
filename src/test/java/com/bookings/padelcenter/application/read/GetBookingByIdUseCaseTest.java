package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetBookingByIdQuery;
import com.bookings.padelcenter.domain.exception.BookingNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.BookingStatus;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.model.Field;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBookingByIdUseCaseTest {

	@Mock
	private BookingRepository bookingRepository;

	@InjectMocks
	private GetBookingByIdUseCase getBookingByIdUseCase;

	private Booking buildBooking(Long id) {
		var user = new User(UUID.randomUUID(), "John", "Doe", "j@test.com",
				"hash", null, Set.of(), Auditable.newAudit());
		var center = new Center(UUID.randomUUID(), "C", "A", "City",
				null, null, null, Auditable.newAudit());
		var field = new Field(UUID.randomUUID(), "Court", "Indoor",
				new BigDecimal("20.00"), true, center, Auditable.newAudit());
		return new Booking(id, user, field,
				LocalDateTime.of(2025, 1, 10, 10, 0),
				LocalDateTime.of(2025, 1, 10, 11, 0),
				new BigDecimal("20.00"), Instant.now(),
				BookingStatus.PENDING, Auditable.newAudit());
	}

	@Test
	@DisplayName("execute_existingId_returnsBooking")
	void execute_existingId_returnsBooking() {
		var bookingId = 42L;
		var booking = buildBooking(bookingId);
		var query = new GetBookingByIdQuery(bookingId);

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

		var result = getBookingByIdUseCase.execute(query);

		assertThat(result).isEqualTo(booking);
		assertThat(result.id()).isEqualTo(bookingId);
		verify(bookingRepository).findById(bookingId);
	}

	@Test
	@DisplayName("execute_nonExistingId_throwsBookingNotFoundException")
	void execute_nonExistingId_throwsBookingNotFoundException() {
		var bookingId = 99L;
		var query = new GetBookingByIdQuery(bookingId);

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> getBookingByIdUseCase.execute(query))
				.isInstanceOf(BookingNotFoundException.class)
				.hasMessageContaining(String.valueOf(bookingId));

		verify(bookingRepository).findById(bookingId);
	}
}
