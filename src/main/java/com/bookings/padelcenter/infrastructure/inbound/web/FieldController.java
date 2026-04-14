package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.query.GetAllFieldsQuery;
import com.bookings.padelcenter.application.query.GetFieldByIdQuery;
import com.bookings.padelcenter.application.read.GetAllFieldsUseCase;
import com.bookings.padelcenter.application.read.GetFieldByIdUseCase;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateFieldResponse;
import com.bookings.padelcenter.infrastructure.inbound.mapper.FieldApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fields")
@RequiredArgsConstructor
public class FieldController {

	private final GetAllFieldsUseCase getAllFieldsUseCase;
	private final GetFieldByIdUseCase getFieldByIdUseCase;
	private final FieldApiMapper fieldApiMapper;

	@GetMapping
	public ResponseEntity<PageResult<CreateFieldResponse>> getAllFields(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		var query = new GetAllFieldsQuery(page, size);
		var fields = getAllFieldsUseCase.execute(query);
		var content = fields.content().stream()
				.map(fieldApiMapper::toResponse)
				.toList();
		var response = new PageResult<>(content, fields.page(), fields.size(), fields.totalElements(), fields.totalPages());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CreateFieldResponse> getFieldById(@PathVariable UUID id) {
		var query = new GetFieldByIdQuery(id);
		var field = getFieldByIdUseCase.execute(query);
		var response = fieldApiMapper.toResponse(field);
		return ResponseEntity.ok(response);
	}
}
