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
	private List<Booking> bookingList;
	private PageResult<Booking> bookingPage;

	private static final int PAGE = 0;
	private static final int SIZE = 10;

	private static final LocalDateTime START_1 = LocalDateTime.of(2024, 12, 25, 10, 0);
	private static final LocalDateTime END_1   = LocalDateTime.of(2024, 12, 25, 11, 0);
	private static final LocalDateTime START_2 = LocalDateTime.of(2024, 12, 26, 10, 0);
	private static final LocalDateTime END_2   = LocalDateTime.of(2024, 12, 26, 11, 0);
	private static final LocalDateTime START_3 = LocalDateTime.of(2024, 12, 27, 10, 0);
	private static final LocalDateTime END_3   = LocalDateTime.of(2024, 12, 27, 11, 0);

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		query = new GetBookingHistoryQuery(userId, PAGE, SIZE);

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

		bookingList = List.of(
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

		bookingPage = new PageResult<>(bookingList, PAGE, SIZE, 3L, 1);
	}

	@Test
	@DisplayName("Should return booking history for existing user")
	void shouldReturnBookingHistoryForExistingUser() {
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId, PAGE, SIZE)).thenReturn(bookingPage);

		PageResult<Booking> result = getBookingHistoryUseCase.execute(query);

		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(3);
		assertThat(result.content()).isEqualTo(bookingList);
		assertThat(result.totalElements()).isEqualTo(3L);

		verify(userRepository, times(1)).findById(userId);
		verify(bookingRepository, times(1)).findByUserId(userId, PAGE, SIZE);
	}

	@Test
	@DisplayName("Should throw UserNotFoundException when user does not exist")
	void shouldThrowExceptionWhenUserNotFound() {
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> getBookingHistoryUseCase.execute(query))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessageContaining(userId.toString());

		verify(userRepository, times(1)).findById(userId);
		verify(bookingRepository, never()).findByUserId(any(), anyInt(), anyInt());
	}

	@Test
	@DisplayName("Should return empty page when user has no bookings")
	void shouldReturnEmptyPageWhenNoBookings() {
		var emptyPage = new PageResult<Booking>(List.of(), PAGE, SIZE, 0L, 0);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId, PAGE, SIZE)).thenReturn(emptyPage);

		PageResult<Booking> result = getBookingHistoryUseCase.execute(query);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEmpty();
		assertThat(result.totalElements()).isEqualTo(0L);

		verify(userRepository, times(1)).findById(userId);
		verify(bookingRepository, times(1)).findByUserId(userId, PAGE, SIZE);
	}

	@Test
	@DisplayName("Should verify user existence before fetching bookings")
	void shouldVerifyUserExistenceFirst() {
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> getBookingHistoryUseCase.execute(query))
			.isInstanceOf(UserNotFoundException.class);

		verify(userRepository).findById(userId);
		verify(bookingRepository, never()).findByUserId(any(), anyInt(), anyInt());
	}

	@Test
	@DisplayName("Should return bookings with different statuses")
	void shouldReturnBookingsWithDifferentStatuses() {
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId, PAGE, SIZE)).thenReturn(bookingPage);

		PageResult<Booking> result = getBookingHistoryUseCase.execute(query);

		assertThat(result.content()).hasSize(3);
		assertThat(result.content().get(0).status()).isEqualTo(BookingStatus.COMPLETED);
		assertThat(result.content().get(1).status()).isEqualTo(BookingStatus.CONFIRMED);
		assertThat(result.content().get(2).status()).isEqualTo(BookingStatus.PENDING);
	}

	@Test
	@DisplayName("Should return bookings with complete details")
	void shouldReturnBookingsWithCompleteDetails() {
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId, PAGE, SIZE)).thenReturn(bookingPage);

		PageResult<Booking> result = getBookingHistoryUseCase.execute(query);

		assertThat(result.content()).isNotEmpty();
		Booking firstBooking = result.content().get(0);
		assertThat(firstBooking.bookingId()).isNotNull();
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
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId, PAGE, SIZE)).thenReturn(bookingPage);

		PageResult<Booking> result = getBookingHistoryUseCase.execute(query);

		assertThat(result.content()).allMatch(booking -> booking.user().userId().equals(userId));
		verify(bookingRepository).findByUserId(userId, PAGE, SIZE);
	}

	@Test
	@DisplayName("Should return single booking when user has only one")
	void shouldReturnSingleBooking() {
		List<Booking> singleBooking = List.of(bookingList.get(0));
		var singlePage = new PageResult<>(singleBooking, PAGE, SIZE, 1L, 1);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId, PAGE, SIZE)).thenReturn(singlePage);

		PageResult<Booking> result = getBookingHistoryUseCase.execute(query);

		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).bookingId()).isEqualTo(1L);
	}

	@Test
	@DisplayName("Should include field and center information in bookings")
	void shouldIncludeFieldAndCenterInformation() {
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId, PAGE, SIZE)).thenReturn(bookingPage);

		PageResult<Booking> result = getBookingHistoryUseCase.execute(query);

		assertThat(result.content()).isNotEmpty();
		Booking firstBooking = result.content().get(0);
		assertThat(firstBooking.field().name()).isEqualTo("Court 1");
		assertThat(firstBooking.field().center().name()).isEqualTo("Padel Center Barcelona");
		assertThat(firstBooking.field().center().city()).isEqualTo("Barcelona");
	}

	@Test
	@DisplayName("Should preserve booking timestamps")
	void shouldPreserveBookingTimestamps() {
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId, PAGE, SIZE)).thenReturn(bookingPage);

		PageResult<Booking> result = getBookingHistoryUseCase.execute(query);

		assertThat(result.content()).hasSize(3);
		assertThat(result.content().get(0).startTime()).isEqualTo(START_1);
		assertThat(result.content().get(0).endTime()).isEqualTo(END_1);
		assertThat(result.content().get(1).startTime()).isEqualTo(START_2);
		assertThat(result.content().get(2).startTime()).isEqualTo(START_3);
	}

	@Test
	@DisplayName("Should preserve booking prices")
	void shouldPreserveBookingPrices() {
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId, PAGE, SIZE)).thenReturn(bookingPage);

		PageResult<Booking> result = getBookingHistoryUseCase.execute(query);

		assertThat(result.content()).allMatch(booking ->
			booking.totalPrice().compareTo(new BigDecimal("25.00")) == 0
		);
	}

	@Test
	@DisplayName("Should call repository findByUserId exactly once")
	void shouldCallRepositoryOnce() {
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(bookingRepository.findByUserId(userId, PAGE, SIZE)).thenReturn(bookingPage);

		getBookingHistoryUseCase.execute(query);

		verify(bookingRepository, times(1)).findByUserId(userId, PAGE, SIZE);
		verifyNoMoreInteractions(bookingRepository);
	}
}
