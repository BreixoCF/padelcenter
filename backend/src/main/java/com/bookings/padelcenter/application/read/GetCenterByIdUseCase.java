package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetCenterByIdQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetCenterByIdUseCase implements QueryUseCase<GetCenterByIdQuery, Center> {

	private final CenterRepository centerRepository;

	@NonNull
	@Override
	public Center execute(GetCenterByIdQuery query) {
		log.debug("center.findById.start centerId={}", query.centerId());
		return centerRepository.findById(query.centerId())
				.orElseThrow(() -> new CenterNotFoundException(query.centerId()));
	}
}
