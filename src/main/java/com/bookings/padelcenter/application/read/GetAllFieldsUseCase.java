package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetAllFieldsQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAllFieldsUseCase implements QueryUseCase<GetAllFieldsQuery, PageResult<Field>> {

	private final FieldRepository fieldRepository;

	@NonNull
	@Override
	public PageResult<Field> execute(GetAllFieldsQuery query) {
		return fieldRepository.findAll(query.page(), query.size());
	}
}
