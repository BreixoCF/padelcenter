package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetFieldAvailabilityQuery;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.model.*;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.domain.repository.FieldRepository;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFieldAvailabilityUseCase")
class GetFieldAvailabilityUseCaseTest {

	@Mock FieldRepository fieldRepository;
	@Mock BookingRepository bookingRepository;
	@InjectMocks GetFieldAvailabilityUseCase useCase;

	private static final UUID FIELD_ID = UUID.randomUUID();
	private static final LocalDate DATE = LocalDate.of(2025, 6, 20);

	private final Field field = new Field(FIELD_ID, "Court 1", "Indoor",
			BigDecimal.valueOf(25), true, null, Auditable.newAudit());

	@Test
	@DisplayName("should return 13 available slots when no bookings exist")
	void getAvailability_noBookings_allSlotsAvailable() {
		// given
		given(fieldRepository.findById(FIELD_ID)).willReturn(Optional.of(field));
		given(bookingRepository.findConfirmedByFieldAndDay(eq(FIELD_ID), any(), any()))
				.willReturn(List.of());

		// when
		var result = useCase.execute(new GetFieldAvailabilityQuery(FIELD_ID, DATE));

		// then
		assertThat(result.fieldId()).isEqualTo(FIELD_ID);
		assertThat(result.date()).isEqualTo(DATE);
		assertThat(result.slots()).hasSize(13);
		assertThat(result.slots()).allMatch(s -> s.available());
		assertThat(result.slots().getFirst().start()).isEqualTo(DATE.atTime(9, 0));
		assertThat(result.slots().getLast().end()).isEqualTo(DATE.atTime(22, 0));
	}

	@Test
	@DisplayName("should mark overlapping slot as unavailable when one booking exists")
	void getAvailability_oneBooking_overlappingSlotUnavailable() {
		// given
		given(fieldRepository.findById(FIELD_ID)).willReturn(Optional.of(field));
		var booking = bookingAt(DATE.atTime(10, 0), DATE.atTime(11, 0));
		given(bookingRepository.findConfirmedByFieldAndDay(eq(FIELD_ID), any(), any()))
				.willReturn(List.of(booking));

		// when
		var result = useCase.execute(new GetFieldAvailabilityQuery(FIELD_ID, DATE));

		// then
		assertThat(result.slots()).hasSize(13);
		// 10:00-11:00 is the second slot (index 1)
		assertThat(result.slots().get(1).start()).isEqualTo(DATE.atTime(10, 0));
		assertThat(result.slots().get(1).available()).isFalse();
		// All other slots should be available
		assertThat(result.slots().stream()
				.filter(s -> !s.start().equals(DATE.atTime(10, 0)))
				.allMatch(s -> s.available())).isTrue();
	}

	@Test
	@DisplayName("should throw FieldNotFoundException when field does not exist")
	void getAvailability_fieldNotFound_throwsFieldNotFoundException() {
		// given
		given(fieldRepository.findById(FIELD_ID)).willReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> useCase.execute(new GetFieldAvailabilityQuery(FIELD_ID, DATE)))
				.isInstanceOf(FieldNotFoundException.class);
	}

	@Test
	@DisplayName("should mark all affected slots as unavailable when booking spans multiple hours")
	void getAvailability_bookingSpanningMultipleSlots_allAffectedUnavailable() {
		// given
		given(fieldRepository.findById(FIELD_ID)).willReturn(Optional.of(field));
		var booking = bookingAt(DATE.atTime(10, 0), DATE.atTime(12, 0));
		given(bookingRepository.findConfirmedByFieldAndDay(eq(FIELD_ID), any(), any()))
				.willReturn(List.of(booking));

		// when
		var result = useCase.execute(new GetFieldAvailabilityQuery(FIELD_ID, DATE));

		// then
		// 10:00-11:00 (index 1) and 11:00-12:00 (index 2) should be unavailable
		assertThat(result.slots().get(1).available()).isFalse();
		assertThat(result.slots().get(2).available()).isFalse();
		// 09:00-10:00 and 12:00-13:00 should be available
		assertThat(result.slots().get(0).available()).isTrue();
		assertThat(result.slots().get(3).available()).isTrue();
	}

	private Booking bookingAt(LocalDateTime start, LocalDateTime end) {
		var user = new User(UUID.randomUUID(), "Test", "User", "test@example.com",
				"hash", "600000001", Set.of(), Auditable.newAudit());
		return new Booking(1L, user, field, start, end,
				BigDecimal.valueOf(25), Instant.now(), BookingStatus.CONFIRMED, Auditable.newAudit());
	}
}
