package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.create.CreateCenterUseCase;
import com.bookings.padelcenter.application.create.CreateFieldUseCase;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateCenterRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateCenterResponse;
import com.bookings.padelcenter.infrastructure.inbound.dto.request.CreateFieldRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.response.CreateFieldResponse;
import com.bookings.padelcenter.infrastructure.inbound.mapper.CenterApiMapper;
import com.bookings.padelcenter.infrastructure.inbound.mapper.FieldApiMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/centers")
@RequiredArgsConstructor
public class CenterController {

	private final CreateCenterUseCase createCenterUseCase;
	private final CreateFieldUseCase createFieldUseCase;
	private final CenterApiMapper centerApiMapper;
	private final FieldApiMapper fieldApiMapper;

	@PostMapping
	public ResponseEntity<@NonNull CreateCenterResponse> createCenter(@RequestBody CreateCenterRequest request) {
		var command = centerApiMapper.toCommand(request);
		var createdCenter = createCenterUseCase.execute(command);
		var response = centerApiMapper.toResponse(createdCenter);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{centerId}/fields")
	public ResponseEntity<@NonNull CreateFieldResponse> createField(@PathVariable UUID centerId,
	                                                                @RequestBody CreateFieldRequest request) {
		var command = fieldApiMapper.toCommand(centerId, request);
		var createdField = createFieldUseCase.execute(command);
		var response = fieldApiMapper.toResponse(createdField);
		return ResponseEntity.ok(response);
	}


}
