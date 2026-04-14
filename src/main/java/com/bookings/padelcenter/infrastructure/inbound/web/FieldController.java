package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateFieldResponse;
import com.bookings.padelcenter.infrastructure.inbound.mapper.FieldApiMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fields")
@RequiredArgsConstructor
public class FieldController {

	private final FieldRepository fieldRepository;
	private final FieldApiMapper fieldApiMapper;

	@GetMapping
	public ResponseEntity<Page<CreateFieldResponse>> getAllFields(Pageable pageable) {
		var fields = fieldRepository.findAll(pageable)
				.map(fieldApiMapper::toResponse);
		return ResponseEntity.ok(fields);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CreateFieldResponse> getFieldById(@PathVariable UUID id) {
		var field = fieldRepository.findById(id)
				.orElseThrow(() -> new FieldNotFoundException(id));
		var response = fieldApiMapper.toResponse(field);
		return ResponseEntity.ok(response);
	}
}
