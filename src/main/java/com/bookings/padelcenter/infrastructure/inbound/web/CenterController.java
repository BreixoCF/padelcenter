package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.create.CreateCenterUseCase;
import com.bookings.padelcenter.application.create.CreateFieldUseCase;
import com.bookings.padelcenter.application.query.GetAllCentersQuery;
import com.bookings.padelcenter.application.read.GetAllCentersUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.CenterApiMapper;
import com.bookings.padelcenter.infrastructure.inbound.mapper.FieldApiMapper;
import com.padelcenter.infrastructure.web.generated.api.CentersApi;
import com.padelcenter.infrastructure.web.generated.model.CenterRequest;
import com.padelcenter.infrastructure.web.generated.model.CenterResponse;
import com.padelcenter.infrastructure.web.generated.model.FieldRequest;
import com.padelcenter.infrastructure.web.generated.model.FieldResponse;
import com.padelcenter.infrastructure.web.generated.model.ListCenters200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CenterController implements CentersApi {

	private final CreateCenterUseCase createCenterUseCase;
	private final GetAllCentersUseCase getAllCentersUseCase;
	private final CreateFieldUseCase createFieldUseCase;
	private final CenterApiMapper centerApiMapper;
	private final FieldApiMapper fieldApiMapper;

	@Override
	public ResponseEntity<CenterResponse> createCenter(CenterRequest centerRequest) {
		var command = centerApiMapper.toCommand(centerRequest);
		var center = createCenterUseCase.execute(command);
		return ResponseEntity.status(HttpStatus.CREATED).body(centerApiMapper.toResponse(center));
	}

	@Override
	public ResponseEntity<ListCenters200Response> listCenters(Integer page, Integer size, String sort) {
		var query = new GetAllCentersQuery(page, size);
		var pageResult = getAllCentersUseCase.execute(query);
		return ResponseEntity.ok(centerApiMapper.toPagedResponse(pageResult));
	}

	@Override
	public ResponseEntity<FieldResponse> createField(UUID centerId, FieldRequest fieldRequest) {
		var command = fieldApiMapper.toCommand(centerId, fieldRequest);
		var field = createFieldUseCase.execute(command);
		return ResponseEntity.status(HttpStatus.CREATED).body(fieldApiMapper.toResponse(field));
	}
}
