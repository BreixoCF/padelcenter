package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetBookingHistoryQuery;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.*;
import com.bookings.padelcenter.domain.repository.BookingRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetBookingHistoryUseCase Tests")
class GetBookingHistoryUseCaseTest {

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private GetBookingHistoryUseCase getBookingHistoryUseCase;

	private UUID userId;
	private GetBookingHistoryQuery query;
	private User user;
	private Center center;
	private Field field;
	private List<Booking> bookings;

	private static final LocalDateTime START_1 = LocalDateTime.of(2024, 12, 25, 10, 0);
	private static final LocalDateTime END_1   = LocalDateTime.of(2024, 12, 25, 11, 0);
	private static final LocalDateTime START_2 = LocalDateTime.of(2024, 12, 26, 10, 0);
	private static final LocalDateTime END_2   = LocalDateTime.of(2024, 12, 26, 11, 0);
	private static final LocalDateTime START_3 = LocalDateTime.of(2024, 12, 27, 10, 0);
	private static final LocalDateTime END_3   = LocalDateTime.of(2024, 12, 27, 11, 0);

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		query = new GetBookingHistoryQuery(userId);

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

		bookings = List.of(
			new Booking(
				1L, user, field, START_1, END_1,
				new BigDecimal("25.00"), Instant.now().minusSeconds(86400),
				BookingStatus.COMPLETED, Auditable.newAudit()
			),
			new Booking(
				2L, user, field, START_2, END_2,
				new BigDecimal("25.00"), Instant.now().minusSeconds(43200),
				BookingStatus.CONFIRMED, Auditable.newAudit()
			),
			new Booking(
				3L, user, field, START_3, END_3,
				new BigDecimal("25.00"), Instant.now(),
				BookingStatus.PENDING, Auditable.newAudit()
			)
		);
	}

	@Test
	@DisplayName("Should return booking history for existing user")
	void shouldReturnBookingHistoryForExistingUser() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId)).thenReturn(bookings);

		// When
		List<Booking> result = getBookingHistoryUseCase.execute(query);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).hasSize(3);
		assertThat(result).isEqualTo(bookings);

		verify(userRepository, times(1)).findById(userId);
		verify(bookingRepository, times(1)).findByUserId(userId);
	}

	@Test
	@DisplayName("Should throw UserNotFoundException when user does not exist")
	void shouldThrowExceptionWhenUserNotFound() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> getBookingHistoryUseCase.execute(query))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessageContaining(userId.toString());

		verify(userRepository, times(1)).findById(userId);
		verify(bookingRepository, never()).findByUserId(any());
	}

	@Test
	@DisplayName("Should return empty list when user has no bookings")
	void shouldReturnEmptyListWhenNoBookings() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId)).thenReturn(new ArrayList<>());

		// When
		List<Booking> result = getBookingHistoryUseCase.execute(query);

		// Then
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();

		verify(userRepository, times(1)).findById(userId);
		verify(bookingRepository, times(1)).findByUserId(userId);
	}

	@Test
	@DisplayName("Should verify user existence before fetching bookings")
	void shouldVerifyUserExistenceFirst() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> getBookingHistoryUseCase.execute(query))
			.isInstanceOf(UserNotFoundException.class);

		verify(userRepository).findById(userId);
		verify(bookingRepository, never()).findByUserId(any());
	}

	@Test
	@DisplayName("Should return bookings with different statuses")
	void shouldReturnBookingsWithDifferentStatuses() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId)).thenReturn(bookings);

		// When
		List<Booking> result = getBookingHistoryUseCase.execute(query);

		// Then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).status()).isEqualTo(BookingStatus.COMPLETED);
		assertThat(result.get(1).status()).isEqualTo(BookingStatus.CONFIRMED);
		assertThat(result.get(2).status()).isEqualTo(BookingStatus.PENDING);
	}

	@Test
	@DisplayName("Should return bookings with complete details")
	void shouldReturnBookingsWithCompleteDetails() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId)).thenReturn(bookings);

		// When
		List<Booking> result = getBookingHistoryUseCase.execute(query);

		// Then
		assertThat(result).isNotEmpty();
		Booking firstBooking = result.get(0);
		assertThat(firstBooking.id()).isNotNull();
		assertThat(firstBooking.user()).isNotNull();
		assertThat(firstBooking.field()).isNotNull();
		assertThat(firstBooking.startTime()).isNotNull();
		assertThat(firstBooking.endTime()).isNotNull();
		assertThat(firstBooking.totalPrice()).isNotNull();
		assertThat(firstBooking.bookedAt()).isNotNull();
		assertThat(firstBooking.status()).isNotNull();
	}

	@Test
	@DisplayName("Should return only bookings for specified user")
	void shouldReturnOnlyBookingsForSpecifiedUser() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId)).thenReturn(bookings);

		// When
		List<Booking> result = getBookingHistoryUseCase.execute(query);

		// Then
		assertThat(result).allMatch(booking -> booking.user().id().equals(userId));
		verify(bookingRepository).findByUserId(userId);
	}

	@Test
	@DisplayName("Should return single booking when user has only one")
	void shouldReturnSingleBooking() {
		// Given
		List<Booking> singleBooking = List.of(bookings.get(0));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId)).thenReturn(singleBooking);

		// When
		List<Booking> result = getBookingHistoryUseCase.execute(query);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).id()).isEqualTo(1L);
	}

	@Test
	@DisplayName("Should include field and center information in bookings")
	void shouldIncludeFieldAndCenterInformation() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId)).thenReturn(bookings);

		// When
		List<Booking> result = getBookingHistoryUseCase.execute(query);

		// Then
		assertThat(result).isNotEmpty();
		Booking firstBooking = result.get(0);
		assertThat(firstBooking.field().name()).isEqualTo("Court 1");
		assertThat(firstBooking.field().center().name()).isEqualTo("Padel Center Barcelona");
		assertThat(firstBooking.field().center().city()).isEqualTo("Barcelona");
	}

	@Test
	@DisplayName("Should preserve booking timestamps")
	void shouldPreserveBookingTimestamps() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId)).thenReturn(bookings);

		// When
		List<Booking> result = getBookingHistoryUseCase.execute(query);

		// Then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).startTime()).isEqualTo(START_1);
		assertThat(result.get(0).endTime()).isEqualTo(END_1);
		assertThat(result.get(1).startTime()).isEqualTo(START_2);
		assertThat(result.get(2).startTime()).isEqualTo(START_3);
	}

	@Test
	@DisplayName("Should preserve booking prices")
	void shouldPreserveBookingPrices() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId)).thenReturn(bookings);

		// When
		List<Booking> result = getBookingHistoryUseCase.execute(query);

		// Then
		assertThat(result).allMatch(booking ->
			booking.totalPrice().compareTo(new BigDecimal("25.00")) == 0
		);
	}

	@Test
	@DisplayName("Should call repository findByUserId exactly once")
	void shouldCallRepositoryOnce() {
		// Given
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId)).thenReturn(bookings);

		// When
		getBookingHistoryUseCase.execute(query);

		// Then
		verify(bookingRepository, times(1)).findByUserId(userId);
		verifyNoMoreInteractions(bookingRepository);
	}
}
