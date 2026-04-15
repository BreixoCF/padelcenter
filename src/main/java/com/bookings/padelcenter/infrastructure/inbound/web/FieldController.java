package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.query.GetAllFieldsQuery;
import com.bookings.padelcenter.application.query.GetFieldByIdQuery;
import com.bookings.padelcenter.application.read.GetAllFieldsUseCase;
import com.bookings.padelcenter.application.read.GetFieldByIdUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.FieldApiMapper;
import com.padelcenter.infrastructure.web.generated.api.FieldsApi;
import com.padelcenter.infrastructure.web.generated.model.FieldResponse;
import com.padelcenter.infrastructure.web.generated.model.ListFields200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FieldController implements FieldsApi {

	private final GetAllFieldsUseCase getAllFieldsUseCase;
	private final GetFieldByIdUseCase getFieldByIdUseCase;
	private final FieldApiMapper fieldApiMapper;

	@Override
	public ResponseEntity<ListFields200Response> listFields(Integer page, Integer size, String sort) {
		var query = new GetAllFieldsQuery(page, size);
		var pageResult = getAllFieldsUseCase.execute(query);
		return ResponseEntity.ok(fieldApiMapper.toPagedResponse(pageResult));
	}

	@Override
	public ResponseEntity<FieldResponse> getFieldById(UUID id) {
		var field = getFieldByIdUseCase.execute(new GetFieldByIdQuery(id));
		return ResponseEntity.ok(fieldApiMapper.toResponse(field));
	}
}
