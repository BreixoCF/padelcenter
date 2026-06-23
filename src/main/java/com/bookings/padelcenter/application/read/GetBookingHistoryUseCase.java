package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetBookingHistoryQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetBookingHistoryUseCase implements QueryUseCase<GetBookingHistoryQuery, PageResult<Booking>> {

	private final BookingRepository bookingRepository;
	private final UserRepository userRepository;

	@Override
	@PreAuthorize("@authResolver.resolveUserId(authentication).equals(#query.userId) or hasRole('ADMIN')")
	public PageResult<Booking> execute(GetBookingHistoryQuery query) {
		log.debug("booking.history.start userId={} page={} size={}",
			query.userId(), query.page(), query.size());

		userRepository.findById(query.userId())
				.orElseThrow(() -> new UserNotFoundException(query.userId()));

		var result = bookingRepository.findByUserId(query.userId(), query.page(), query.size());

		log.debug("booking.history.found userId={} count={} totalPages={}",
			query.userId(), result.content().size(), result.totalPages());

		return result;
	}
}
