package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.CancelBookingCommand;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.exception.BookingAlreadyCancelledException;
import com.bookings.padelcenter.domain.exception.BookingNotFoundException;
import com.bookings.padelcenter.domain.model.*;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelBookingUseCase Tests")
class CancelBookingUseCaseTest {

	@Mock
	private BookingRepository bookingRepository;

	@InjectMocks
	private CancelBookingUseCase cancelBookingUseCase;

	private Long bookingId;
	private UUID modifiedBy;
	private CancelBookingCommand command;
	private Booking existingBooking;
	private User user;
	private Field field;
	private Center center;

	private static final LocalDateTime START_TIME = LocalDateTime.of(2024, 12, 25, 10, 0);
	private static final LocalDateTime END_TIME   = LocalDateTime.of(2024, 12, 25, 11, 0);

	@BeforeEach
	void setUp() {
		bookingId = 1L;
		modifiedBy = UUID.randomUUID();

		command = new CancelBookingCommand(bookingId, new AuthenticatedUser(modifiedBy));

		UUID userId = UUID.randomUUID();
		user = new User(
			userId,
			null,
			"John",
			"Doe",
			"john.doe@example.com",
			"hashedPassword",
			"600123456",
			Set.of(),
			Auditable.newAudit()
		);

		UUID centerId = UUID.randomUUID();
		center = new Center(
			centerId,
			"Padel Center Barcelona",
			"Calle Principal 123",
			"Barcelona",
			"933123456",
			"barcelona@padelcenter.com",
			null,
			Auditable.newAudit()
		);

		UUID fieldId = UUID.randomUUID();
		field = new Field(
			fieldId,
			"Court 1",
			"Indoor",
			new BigDecimal("25.00"),
			true,
			center,
			Auditable.newAudit()
		);

		existingBooking = new Booking(
			bookingId,
			user,
			field,
			START_TIME,
			END_TIME,
			new BigDecimal("25.00"),
			Instant.now(),
			BookingStatus.PENDING,
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("Should successfully cancel a booking")
	void shouldCancelBooking() {
		// Given
		Booking cancelledBooking = existingBooking.cancel(modifiedBy);
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenReturn(cancelledBooking);

		// When
		Booking result = cancelBookingUseCase.execute(command);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
		assertThat(result.bookingId()).isEqualTo(bookingId);

		verify(bookingRepository, times(1)).findById(bookingId);
		verify(bookingRepository, times(1)).save(any(Booking.class));
	}

	@Test
	@DisplayName("Should throw BookingNotFoundException when booking does not exist")
	void shouldThrowExceptionWhenBookingNotFound() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> cancelBookingUseCase.execute(command))
			.isInstanceOf(BookingNotFoundException.class)
			.hasMessageContaining(bookingId.toString());

		verify(bookingRepository, times(1)).findById(bookingId);
		verify(bookingRepository, never()).save(any(Booking.class));
	}

	@Test
	@DisplayName("Should throw BookingAlreadyCancelledException when booking is already cancelled")
	void shouldThrowExceptionWhenBookingAlreadyCancelled() {
		// Given
		Booking alreadyCancelledBooking = existingBooking.cancel(modifiedBy);
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(alreadyCancelledBooking));

		// When & Then
		assertThatThrownBy(() -> cancelBookingUseCase.execute(command))
			.isInstanceOf(BookingAlreadyCancelledException.class)
			.hasMessageContaining(bookingId.toString());

		verify(bookingRepository, times(1)).findById(bookingId);
		verify(bookingRepository, never()).save(any(Booking.class));
	}

	@Test
	@DisplayName("Should update status to CANCELLED")
	void shouldUpdateStatusToCancelled() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		cancelBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.status()).isEqualTo(BookingStatus.CANCELLED);
	}

	@Test
	@DisplayName("Should update audit modifiedBy field")
	void shouldUpdateModifiedByField() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		cancelBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.audit().modifiedBy()).isEqualTo(modifiedBy);
		assertThat(capturedBooking.audit().modifiedAt()).isNotNull();
	}

	@Test
	@DisplayName("Should preserve booking details during cancellation")
	void shouldPreserveBookingDetails() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		cancelBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.bookingId()).isEqualTo(existingBooking.bookingId());
		assertThat(capturedBooking.user()).isEqualTo(existingBooking.user());
		assertThat(capturedBooking.field()).isEqualTo(existingBooking.field());
		assertThat(capturedBooking.startTime()).isEqualTo(existingBooking.startTime());
		assertThat(capturedBooking.endTime()).isEqualTo(existingBooking.endTime());
		assertThat(capturedBooking.totalPrice()).isEqualTo(existingBooking.totalPrice());
		assertThat(capturedBooking.bookedAt()).isEqualTo(existingBooking.bookedAt());
	}

	@Test
	@DisplayName("Should preserve audit creation fields during cancellation")
	void shouldPreserveAuditCreationFields() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		cancelBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.audit().createdBy()).isEqualTo(existingBooking.audit().createdBy());
		assertThat(capturedBooking.audit().createdAt()).isEqualTo(existingBooking.audit().createdAt());
	}

	@Test
	@DisplayName("Should return the saved cancelled booking")
	void shouldReturnSavedCancelledBooking() {
		// Given
		Booking cancelledBooking = existingBooking.cancel(modifiedBy);
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenReturn(cancelledBooking);

		// When
		Booking result = cancelBookingUseCase.execute(command);

		// Then
		assertThat(result.bookingId()).isEqualTo(existingBooking.bookingId());
		assertThat(result.user()).isEqualTo(existingBooking.user());
		assertThat(result.field()).isEqualTo(existingBooking.field());
		assertThat(result.startTime()).isEqualTo(existingBooking.startTime());
		assertThat(result.endTime()).isEqualTo(existingBooking.endTime());
		assertThat(result.totalPrice()).isEqualTo(existingBooking.totalPrice());
		assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
	}

	@Test
	@DisplayName("Should cancel booking with PENDING status")
	void shouldCancelPendingBooking() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Booking result = cancelBookingUseCase.execute(command);

		// Then
		assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
		verify(bookingRepository).save(any(Booking.class));
	}

	@Test
	@DisplayName("Should cancel booking with CONFIRMED status")
	void shouldCancelConfirmedBooking() {
		// Given
		Booking confirmedBooking = new Booking(
			bookingId, user, field, START_TIME, END_TIME,
			new BigDecimal("25.00"), Instant.now(), BookingStatus.CONFIRMED, Auditable.newAudit()
		);

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(confirmedBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Booking result = cancelBookingUseCase.execute(command);

		// Then
		assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
		verify(bookingRepository).save(any(Booking.class));
	}

	@Test
	@DisplayName("Should verify booking is not cancelled before attempting cancellation")
	void shouldVerifyBookingIsNotCancelled() {
		// Given
		Booking alreadyCancelledBooking = new Booking(
			bookingId, user, field, START_TIME, END_TIME,
			new BigDecimal("25.00"), Instant.now(), BookingStatus.CANCELLED, Auditable.newAudit()
		);

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(alreadyCancelledBooking));

		// When & Then
		assertThatThrownBy(() -> cancelBookingUseCase.execute(command))
			.isInstanceOf(BookingAlreadyCancelledException.class);

		verify(bookingRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should use isCancelled method to check booking status")
	void shouldUseIsCancelledMethod() {
		// Given
		Booking cancelledBooking = existingBooking.cancel(UUID.randomUUID());

		// When
		boolean isCancelled = cancelledBooking.isCancelled();

		// Then
		assertThat(isCancelled).isTrue();
	}

	@Test
	@DisplayName("Should not be cancelled for non-cancelled status")
	void shouldNotBeCancelledForNonCancelledStatus() {
		// When
		boolean isCancelled = existingBooking.isCancelled();

		// Then
		assertThat(isCancelled).isFalse();
	}
}
