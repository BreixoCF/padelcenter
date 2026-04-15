package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetBookingHistoryQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetBookingHistoryUseCase implements QueryUseCase<GetBookingHistoryQuery, PageResult<Booking>> {

	private final BookingRepository bookingRepository;
	private final UserRepository userRepository;

	@Override
	public PageResult<Booking> execute(GetBookingHistoryQuery query) {
		userRepository.findById(query.userId())
				.orElseThrow(() -> new UserNotFoundException(query.userId()));

		return bookingRepository.findByUserId(query.userId(), query.page(), query.size());
	}
}
