package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetFieldsByCenterQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetFieldsByCenterUseCase implements QueryUseCase<GetFieldsByCenterQuery, PageResult<Field>> {

	private final CenterRepository centerRepository;
	private final FieldRepository fieldRepository;

	@NonNull
	@Override
	public PageResult<Field> execute(GetFieldsByCenterQuery query) {
		log.debug("fields.byCenter.start centerId={} type={} available={} page={} size={}",
				query.centerId(), query.type(), query.available(), query.page(), query.size());

		centerRepository.findById(query.centerId())
				.orElseThrow(() -> new CenterNotFoundException(query.centerId()));

		var result = fieldRepository.findByCenterWithFilters(
				query.centerId(), query.type(), query.available(), query.page(), query.size());

		log.debug("fields.byCenter.done centerId={} found={}",
				query.centerId(), result.totalElements());

		return result;
	}
}
