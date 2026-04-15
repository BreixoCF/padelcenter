package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetAllCentersQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAllCentersUseCase implements QueryUseCase<GetAllCentersQuery, PageResult<Center>> {

	private final CenterRepository centerRepository;

	@NonNull
	@Override
	public PageResult<Center> execute(GetAllCentersQuery query) {
		return centerRepository.findAll(query.page(), query.size());
	}
}
