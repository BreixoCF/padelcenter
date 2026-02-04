package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateBookingCommand;
import com.bookings.padelcenter.application.mapper.BookingCommandMapper;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.*;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import com.bookings.padelcenter.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("CreateBookingUseCase Tests")
class CreateBookingUseCaseTest {

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private FieldRepository fieldRepository;

	@Mock
	private BookingCommandMapper bookingCommandMapper;

	@InjectMocks
	private CreateBookingUseCase createBookingUseCase;

	private UUID userId;
	private UUID fieldId;
	private UUID centerId;
	private CreateBookingCommand command;
	private User user;
	private Field field;
	private Center center;
	private Booking expectedBooking;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		fieldId = UUID.randomUUID();
		centerId = UUID.randomUUID();

		command = new CreateBookingCommand(
			userId,
			fieldId,
			"2024-12-25T10:00:00Z",
			"2024-12-25T11:00:00Z",
			new BigDecimal("25.00")
		);

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

		field = new Field(
			fieldId,
			"Court 1",
			"Indoor",
			new BigDecimal("25.00"),
			true,
			center,
			Auditable.newAudit()
		);

		expectedBooking = new Booking(
			null,
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
	@DisplayName("Should successfully create a booking")
	void shouldCreateBooking() {
		// Given
		Booking savedBooking = new Booking(
			1L,
			user,
			field,
			"2024-12-25T10:00:00Z",
			"2024-12-25T11:00:00Z",
			new BigDecimal("25.00"),
			Instant.now(),
			BookingStatus.PENDING,
			Auditable.newAudit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
		when(bookingCommandMapper.toDomain(command, user, field)).thenReturn(expectedBooking);
		when(bookingRepository.save(expectedBooking)).thenReturn(savedBooking);

		// When
		Booking result = createBookingUseCase.execute(command);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.user()).isEqualTo(user);
		assertThat(result.field()).isEqualTo(field);
		assertThat(result.startTime()).isEqualTo("2024-12-25T10:00:00Z");
		assertThat(result.endTime()).isEqualTo("2024-12-25T11:00:00Z");
		assertThat(result.totalPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
		assertThat(result.status()).isEqualTo(BookingStatus.PENDING);

		verify(userRepository, times(1)).findById(userId);
		verify(fieldRepository, times(1)).findById(fieldId);
		verify(bookingCommandMapper, times(1)).toDomain(command, user, field);
		verify(bookingRepository, times(1)).save(expectedBooking);
	}

	@Test
	@DisplayName("Should throw UserNotFoundException when user does not exist")
	void shouldThrowExceptionWhenUserNotFound() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> createBookingUseCase.execute(command))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessageContaining(userId.toString());

		verify(userRepository, times(1)).findById(userId);
		verify(fieldRepository, never()).findById(any());
		verify(bookingCommandMapper, never()).toDomain(any(), any(), any());
		verify(bookingRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should throw FieldNotFoundException when field does not exist")
	void shouldThrowExceptionWhenFieldNotFound() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> createBookingUseCase.execute(command))
			.isInstanceOf(FieldNotFoundException.class)
			.hasMessageContaining(fieldId.toString());

		verify(userRepository, times(1)).findById(userId);
		verify(fieldRepository, times(1)).findById(fieldId);
		verify(bookingCommandMapper, never()).toDomain(any(), any(), any());
		verify(bookingRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should use mapper to create booking from command")
	void shouldUseMapperToCreateBooking() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
		when(bookingCommandMapper.toDomain(command, user, field)).thenReturn(expectedBooking);
		when(bookingRepository.save(any(Booking.class))).thenReturn(expectedBooking);

		// When
		createBookingUseCase.execute(command);

		// Then
		verify(bookingCommandMapper).toDomain(command, user, field);
	}

	@Test
	@DisplayName("Should save booking using repository")
	void shouldSaveBookingUsingRepository() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
		when(bookingCommandMapper.toDomain(command, user, field)).thenReturn(expectedBooking);
		when(bookingRepository.save(expectedBooking)).thenReturn(expectedBooking);

		// When
		createBookingUseCase.execute(command);

		// Then
		verify(bookingRepository).save(expectedBooking);
	}

	@Test
	@DisplayName("Should return the saved booking")
	void shouldReturnSavedBooking() {
		// Given
		Booking savedBooking = new Booking(
			1L,
			user,
			field,
			"2024-12-25T10:00:00Z",
			"2024-12-25T11:00:00Z",
			new BigDecimal("25.00"),
			Instant.now(),
			BookingStatus.PENDING,
			Auditable.newAudit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
		when(bookingCommandMapper.toDomain(command, user, field)).thenReturn(expectedBooking);
		when(bookingRepository.save(expectedBooking)).thenReturn(savedBooking);

		// When
		Booking result = createBookingUseCase.execute(command);

		// Then
		assertThat(result).isEqualTo(savedBooking);
		assertThat(result.id()).isNotNull();
		assertThat(result.id()).isEqualTo(1L);
	}

	@Test
	@DisplayName("Should validate user existence before field")
	void shouldValidateUserBeforeField() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> createBookingUseCase.execute(command))
			.isInstanceOf(UserNotFoundException.class);

		verify(userRepository).findById(userId);
		verify(fieldRepository, never()).findById(any());
	}

	@Test
	@DisplayName("Should create booking with PENDING status")
	void shouldCreateBookingWithPendingStatus() {
		// Given
		Booking savedBooking = new Booking(
			1L,
			user,
			field,
			"2024-12-25T10:00:00Z",
			"2024-12-25T11:00:00Z",
			new BigDecimal("25.00"),
			Instant.now(),
			BookingStatus.PENDING,
			Auditable.newAudit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
		when(bookingCommandMapper.toDomain(command, user, field)).thenReturn(expectedBooking);
		when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

		// When
		Booking result = createBookingUseCase.execute(command);

		// Then
		assertThat(result.status()).isEqualTo(BookingStatus.PENDING);
	}

	@Test
	@DisplayName("Should associate booking with correct user and field")
	void shouldAssociateBookingWithUserAndField() {
		// Given
		Booking savedBooking = new Booking(
			1L,
			user,
			field,
			"2024-12-25T10:00:00Z",
			"2024-12-25T11:00:00Z",
			new BigDecimal("25.00"),
			Instant.now(),
			BookingStatus.PENDING,
			Auditable.newAudit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
		when(bookingCommandMapper.toDomain(command, user, field)).thenReturn(expectedBooking);
		when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

		// When
		Booking result = createBookingUseCase.execute(command);

		// Then
		assertThat(result.user().id()).isEqualTo(userId);
		assertThat(result.field().id()).isEqualTo(fieldId);
	}

	@Test
	@DisplayName("Should preserve booking time details")
	void shouldPreserveBookingTimeDetails() {
		// Given
		Booking savedBooking = new Booking(
			1L,
			user,
			field,
			"2024-12-25T10:00:00Z",
			"2024-12-25T11:00:00Z",
			new BigDecimal("25.00"),
			Instant.now(),
			BookingStatus.PENDING,
			Auditable.newAudit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
		when(bookingCommandMapper.toDomain(command, user, field)).thenReturn(expectedBooking);
		when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

		// When
		Booking result = createBookingUseCase.execute(command);

		// Then
		assertThat(result.startTime()).isEqualTo("2024-12-25T10:00:00Z");
		assertThat(result.endTime()).isEqualTo("2024-12-25T11:00:00Z");
		assertThat(result.totalPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
	}

	@Test
	@DisplayName("Should set bookedAt timestamp when creating booking")
	void shouldSetBookedAtTimestamp() {
		// Given
		Instant bookedAt = Instant.now();
		Booking savedBooking = new Booking(
			1L,
			user,
			field,
			"2024-12-25T10:00:00Z",
			"2024-12-25T11:00:00Z",
			new BigDecimal("25.00"),
			bookedAt,
			BookingStatus.PENDING,
			Auditable.newAudit()
		);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));
		when(bookingCommandMapper.toDomain(command, user, field)).thenReturn(expectedBooking);
		when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

		// When
		Booking result = createBookingUseCase.execute(command);

		// Then
		assertThat(result.bookedAt()).isNotNull();
		assertThat(result.bookedAt()).isEqualTo(bookedAt);
	}
}
