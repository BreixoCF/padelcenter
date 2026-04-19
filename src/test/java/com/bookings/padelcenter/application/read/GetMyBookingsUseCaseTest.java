package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetMyBookingsQuery;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.BookingStatus;
import com.bookings.padelcenter.domain.model.PageResult;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMyBookingsUseCase Tests")
class GetMyBookingsUseCaseTest {

	@Mock
	private BookingRepository bookingRepository;

	@InjectMocks
	private GetMyBookingsUseCase getMyBookingsUseCase;

	private Booking makeBooking(UUID userId, BookingStatus status) {
		return new Booking(1L, null, null,
			LocalDateTime.now(), LocalDateTime.now().plusHours(1),
			BigDecimal.valueOf(25.0), Instant.now(), status, null);
	}

	@Test
	@DisplayName("getMyBookings_noFilter_returnsAllBookings")
	void getMyBookings_noFilter_returnsAllBookings() {
		var userId = UUID.randomUUID();
		var booking = makeBooking(userId, BookingStatus.CONFIRMED);
		var expected = new PageResult<>(List.of(booking), 0, 10, 1L, 1);

		when(bookingRepository.findByUserIdAndStatus(userId, null, 0, 10)).thenReturn(expected);

		var result = getMyBookingsUseCase.execute(new GetMyBookingsQuery(userId, null, 0, 10));

		assertThat(result.content()).hasSize(1);
		assertThat(result.totalElements()).isEqualTo(1L);
		verify(bookingRepository).findByUserIdAndStatus(userId, null, 0, 10);
	}

	@Test
	@DisplayName("getMyBookings_withStatusFilter_returnsFiltered")
	void getMyBookings_withStatusFilter_returnsFiltered() {
		var userId = UUID.randomUUID();
		var booking = makeBooking(userId, BookingStatus.CANCELLED);
		var expected = new PageResult<>(List.of(booking), 0, 10, 1L, 1);

		when(bookingRepository.findByUserIdAndStatus(userId, "CANCELLED", 0, 10)).thenReturn(expected);

		var result = getMyBookingsUseCase.execute(new GetMyBookingsQuery(userId, "CANCELLED", 0, 10));

		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).status()).isEqualTo(BookingStatus.CANCELLED);
		verify(bookingRepository).findByUserIdAndStatus(userId, "CANCELLED", 0, 10);
	}

	@Test
	@DisplayName("getMyBookings_noBookings_returnsEmpty")
	void getMyBookings_noBookings_returnsEmpty() {
		var userId = UUID.randomUUID();
		var empty = new PageResult<Booking>(List.of(), 0, 10, 0L, 0);

		when(bookingRepository.findByUserIdAndStatus(userId, null, 0, 10)).thenReturn(empty);

		var result = getMyBookingsUseCase.execute(new GetMyBookingsQuery(userId, null, 0, 10));

		assertThat(result.content()).isEmpty();
		assertThat(result.totalElements()).isZero();
		verify(bookingRepository).findByUserIdAndStatus(userId, null, 0, 10);
	}
}
