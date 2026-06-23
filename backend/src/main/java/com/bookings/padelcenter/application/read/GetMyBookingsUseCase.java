package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetMyBookingsQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyBookingsUseCase implements QueryUseCase<GetMyBookingsQuery, PageResult<Booking>> {

	private final BookingRepository bookingRepository;

	@NonNull
	@Override
	public PageResult<Booking> execute(GetMyBookingsQuery query) {
		log.debug("booking.my.list.start userId={} status={} page={} size={}",
			query.userId(), query.status(), query.page(), query.size());

		var result = bookingRepository.findByUserIdAndStatus(
				query.userId(), query.status(), query.page(), query.size());

		log.debug("booking.my.list.found count={} totalPages={} userId={}",
			result.content().size(), result.totalPages(), query.userId());

		return result;
	}
}
