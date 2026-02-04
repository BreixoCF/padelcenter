package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetBookingHistoryQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetBookingHistoryUseCase implements QueryUseCase<GetBookingHistoryQuery, List<Booking>> {

	private final BookingRepository bookingRepository;
	private final UserRepository userRepository;

	@Override
	public List<Booking> execute(GetBookingHistoryQuery query) {
		// Verify user exists
		userRepository.findById(query.userId())
				.orElseThrow(() -> new UserNotFoundException(query.userId()));

		return bookingRepository.findByUserId(query.userId());
	}
}
