package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetFieldByIdQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetFieldByIdUseCase implements QueryUseCase<GetFieldByIdQuery, Field> {

	private final FieldRepository fieldRepository;

	@NonNull
	@Override
	public Field execute(GetFieldByIdQuery query) {
		return fieldRepository.findById(query.fieldId())
				.orElseThrow(() -> new FieldNotFoundException(query.fieldId()));
	}
}
