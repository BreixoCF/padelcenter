package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetCenterBookingsQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.Booking;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.BookingRepository;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetCenterBookingsUseCase implements QueryUseCase<GetCenterBookingsQuery, PageResult<Booking>> {

	private final CenterRepository centerRepository;
	private final BookingRepository bookingRepository;

	@NonNull
	@Override
	public PageResult<Booking> execute(GetCenterBookingsQuery query) {
		centerRepository.findById(query.centerId())
				.orElseThrow(() -> new CenterNotFoundException(query.centerId()));

		LocalDateTime dayStart = null;
		LocalDateTime dayEnd = null;

		if (query.date() != null) {
			dayStart = query.date().atStartOfDay();
			dayEnd = query.date().plusDays(1).atStartOfDay();
		} else if (query.startDate() != null || query.endDate() != null) {
			dayStart = query.startDate() != null ? query.startDate().atStartOfDay() : null;
			dayEnd = query.endDate() != null ? query.endDate().atStartOfDay() : null;
		}

		log.debug("bookings.byCenter.start centerId={} fieldId={} page={} size={}",
				query.centerId(), query.fieldId(), query.page(), query.size());

		var result = bookingRepository.findByCenterWithFilters(
				query.centerId(), query.fieldId(), dayStart, dayEnd, query.status(), query.page(), query.size());

		log.debug("bookings.byCenter.done centerId={} found={}", query.centerId(), result.totalElements());

		return result;
	}
}
