package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateBookingCommand;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateBookingUseCase Tests")
class UpdateBookingUseCaseTest {

	@Mock
	private BookingRepository bookingRepository;

	@InjectMocks
	private UpdateBookingUseCase updateBookingUseCase;

	private Long bookingId;
	private UUID modifiedBy;
	private UpdateBookingCommand command;
	private Booking existingBooking;
	private User user;
	private Field field;
	private Center center;

	@BeforeEach
	void setUp() {
		bookingId = 1L;
		modifiedBy = UUID.randomUUID();

		command = new UpdateBookingCommand(
			bookingId,
			"2024-12-26T10:00:00Z",
			"2024-12-26T11:00:00Z",
			new BigDecimal("30.00"),
			modifiedBy
		);

		UUID userId = UUID.randomUUID();
		user = new User(
			userId,
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
			"2024-12-25T10:00:00Z",
			"2024-12-25T11:00:00Z",
			new BigDecimal("25.00"),
			Instant.now(),
			BookingStatus.PENDING,
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("Should successfully update booking")
	void shouldUpdateBooking() {
		// Given
		Booking updatedBooking = existingBooking.updateDetails(
			command.startTime(),
			command.endTime(),
			command.totalPrice(),
			modifiedBy
		);

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);

		// When
		Booking result = updateBookingUseCase.execute(command);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.startTime()).isEqualTo("2024-12-26T10:00:00Z");
		assertThat(result.endTime()).isEqualTo("2024-12-26T11:00:00Z");
		assertThat(result.totalPrice()).isEqualByComparingTo(new BigDecimal("30.00"));

		verify(bookingRepository, times(1)).findById(bookingId);
		verify(bookingRepository, times(1)).save(any(Booking.class));
	}

	@Test
	@DisplayName("Should throw BookingNotFoundException when booking does not exist")
	void shouldThrowExceptionWhenBookingNotFound() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> updateBookingUseCase.execute(command))
			.isInstanceOf(BookingNotFoundException.class)
			.hasMessageContaining(bookingId.toString());

		verify(bookingRepository, times(1)).findById(bookingId);
		verify(bookingRepository, never()).save(any(Booking.class));
	}

	@Test
	@DisplayName("Should update audit modifiedBy field")
	void shouldUpdateModifiedByField() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		updateBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.audit().modifiedBy()).isEqualTo(modifiedBy);
		assertThat(capturedBooking.audit().modifiedAt()).isNotNull();
	}

	@Test
	@DisplayName("Should preserve booking ID and user during update")
	void shouldPreserveBookingIdAndUser() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		updateBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.id()).isEqualTo(bookingId);
		assertThat(capturedBooking.user()).isEqualTo(existingBooking.user());
		assertThat(capturedBooking.field()).isEqualTo(existingBooking.field());
	}

	@Test
	@DisplayName("Should preserve booking status during update")
	void shouldPreserveBookingStatus() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		updateBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.status()).isEqualTo(BookingStatus.PENDING);
	}

	@Test
	@DisplayName("Should preserve bookedAt timestamp during update")
	void shouldPreserveBookedAtTimestamp() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		updateBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.bookedAt()).isEqualTo(existingBooking.bookedAt());
	}

	@Test
	@DisplayName("Should update only the specified fields")
	void shouldUpdateOnlySpecifiedFields() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		updateBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.startTime()).isEqualTo("2024-12-26T10:00:00Z");
		assertThat(capturedBooking.endTime()).isEqualTo("2024-12-26T11:00:00Z");
		assertThat(capturedBooking.totalPrice()).isEqualByComparingTo(new BigDecimal("30.00"));
	}

	@Test
	@DisplayName("Should return the saved updated booking")
	void shouldReturnSavedUpdatedBooking() {
		// Given
		Booking updatedBooking = existingBooking.updateDetails(
			command.startTime(),
			command.endTime(),
			command.totalPrice(),
			modifiedBy
		);

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);

		// When
		Booking result = updateBookingUseCase.execute(command);

		// Then
		assertThat(result).isEqualTo(updatedBooking);
	}

	@Test
	@DisplayName("Should update booking with different price")
	void shouldUpdateBookingWithDifferentPrice() {
		// Given
		UpdateBookingCommand commandWithDifferentPrice = new UpdateBookingCommand(
			bookingId,
			"2024-12-25T14:00:00Z",
			"2024-12-25T16:00:00Z",
			new BigDecimal("50.00"),
			modifiedBy
		);

		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		updateBookingUseCase.execute(commandWithDifferentPrice);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.totalPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
	}

	@Test
	@DisplayName("Should preserve audit creation fields during update")
	void shouldPreserveAuditCreationFields() {
		// Given
		when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);

		// When
		updateBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.audit().createdBy()).isEqualTo(existingBooking.audit().createdBy());
		assertThat(capturedBooking.audit().createdAt()).isEqualTo(existingBooking.audit().createdAt());
	}
}
