package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetCenterBookingsQuery;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.*;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCenterBookingsUseCase")
class GetCenterBookingsUseCaseTest {

	@Mock CenterRepository centerRepository;
	@Mock BookingRepository bookingRepository;
	@InjectMocks GetCenterBookingsUseCase useCase;

	private static final UUID CENTER_ID = UUID.randomUUID();
	private static final UUID FIELD_ID = UUID.randomUUID();
	private final Center center = new Center(CENTER_ID, "Padel BCN", "Calle 1", "Barcelona",
			"933000001", "bcn@padel.com", null, Auditable.newAudit());

	@Test
	@DisplayName("should return all bookings when no filters are provided")
	void getCenterBookings_noFilters_returnsAllBookings() {
		var bookings = List.of(booking(), booking());
		var page = new PageResult<>(bookings, 0, 20, 2L, 1);
		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
		given(bookingRepository.findByCenterWithFilters(CENTER_ID, null, null, null, 0, 20)).willReturn(page);

		var result = useCase.execute(new GetCenterBookingsQuery(CENTER_ID, null, null, null, null, 0, 20));

		assertThat(result.content()).hasSize(2);
		assertThat(result.totalElements()).isEqualTo(2L);
	}

	@Test
	@DisplayName("should filter by specific day when date is provided")
	void getCenterBookings_withDateFilter_returnsDayBookings() {
		var date = LocalDate.of(2025, 6, 20);
		var expectedStart = date.atStartOfDay();
		var expectedEnd = date.plusDays(1).atStartOfDay();
		var bookings = List.of(booking());
		var page = new PageResult<>(bookings, 0, 20, 1L, 1);

		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
		given(bookingRepository.findByCenterWithFilters(CENTER_ID, null, expectedStart, expectedEnd, 0, 20))
				.willReturn(page);

		var result = useCase.execute(new GetCenterBookingsQuery(CENTER_ID, null, date, null, null, 0, 20));

		assertThat(result.content()).hasSize(1);
	}

	@Test
	@DisplayName("should filter by date range when startDate and endDate are provided")
	void getCenterBookings_withDateRange_returnsRangeBookings() {
		var startDate = LocalDate.of(2025, 6, 1);
		var endDate = LocalDate.of(2025, 6, 30);
		var bookings = List.of(booking(), booking(), booking());
		var page = new PageResult<>(bookings, 0, 20, 3L, 1);

		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
		given(bookingRepository.findByCenterWithFilters(
				CENTER_ID, null, startDate.atStartOfDay(), endDate.atStartOfDay(), 0, 20))
				.willReturn(page);

		var result = useCase.execute(
				new GetCenterBookingsQuery(CENTER_ID, null, null, startDate, endDate, 0, 20));

		assertThat(result.content()).hasSize(3);
	}

	@Test
	@DisplayName("should filter by field when fieldId is provided")
	void getCenterBookings_withFieldFilter_returnsFieldBookings() {
		var bookings = List.of(booking());
		var page = new PageResult<>(bookings, 0, 20, 1L, 1);

		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
		given(bookingRepository.findByCenterWithFilters(CENTER_ID, FIELD_ID, null, null, 0, 20))
				.willReturn(page);

		var result = useCase.execute(new GetCenterBookingsQuery(CENTER_ID, FIELD_ID, null, null, null, 0, 20));

		assertThat(result.content()).hasSize(1);
	}

	@Test
	@DisplayName("should throw CenterNotFoundException when center does not exist")
	void getCenterBookings_centerNotFound_throwsCenterNotFoundException() {
		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(
				new GetCenterBookingsQuery(CENTER_ID, null, null, null, null, 0, 20)))
				.isInstanceOf(CenterNotFoundException.class);
	}

	private Booking booking() {
		var field = new Field(FIELD_ID, "Pista 1", "Indoor", BigDecimal.valueOf(25), true, center, Auditable.newAudit());
		var user = new User(UUID.randomUUID(), "Test", "User", "test@test.com",
				"hash", "600000000", java.util.Set.of(), Auditable.newAudit());
		return new Booking(1L, user, field,
				LocalDateTime.of(2025, 6, 20, 10, 0),
				LocalDateTime.of(2025, 6, 20, 11, 0),
				BigDecimal.valueOf(25), Instant.now(), BookingStatus.CONFIRMED, Auditable.newAudit());
	}
}
