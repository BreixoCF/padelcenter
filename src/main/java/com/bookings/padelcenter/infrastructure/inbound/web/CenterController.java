package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.create.CreateCenterUseCase;
import com.bookings.padelcenter.infrastructure.inbound.dto.CreateCenterRequest;
import com.bookings.padelcenter.infrastructure.inbound.dto.CreateCenterResponse;
import com.bookings.padelcenter.infrastructure.inbound.mapper.CenterApiMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/centers")
@RequiredArgsConstructor
public class CenterController {

	private final CreateCenterUseCase createCenterUseCase;

	private final CenterApiMapper mapper;

	@PostMapping
	public ResponseEntity<@NonNull CreateCenterResponse> createCenter(@RequestBody CreateCenterRequest request) {
		var command = mapper.toCommand(request);
		var createdCenter = createCenterUseCase.execute(command);
		var response = mapper.toResponse(createdCenter);
		return ResponseEntity.ok(response);
	}

}
