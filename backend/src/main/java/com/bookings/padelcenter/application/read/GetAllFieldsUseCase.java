package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetAllFieldsQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.model.PageResult;
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
public class GetAllFieldsUseCase implements QueryUseCase<GetAllFieldsQuery, PageResult<Field>> {

	private final FieldRepository fieldRepository;

	@NonNull
	@Override
	public PageResult<Field> execute(GetAllFieldsQuery query) {
		log.debug("fields.list.start page={} size={}",
			query.page(), query.size());
		var result = fieldRepository.findAll(query.page(), query.size());
		log.debug("fields.list.found count={} totalPages={}",
			result.content().size(), result.totalPages());
		return result;
	}
}
