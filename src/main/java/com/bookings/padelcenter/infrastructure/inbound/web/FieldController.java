package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.command.DeleteFieldCommand;
import com.bookings.padelcenter.application.command.UpdateFieldAvailabilityCommand;
import com.bookings.padelcenter.application.command.UpdateFieldCommand;
import com.bookings.padelcenter.application.delete.DeleteFieldUseCase;
import com.bookings.padelcenter.application.query.GetAllFieldsQuery;
import com.bookings.padelcenter.application.query.GetFieldAvailabilityQuery;
import com.bookings.padelcenter.application.query.GetFieldByIdQuery;
import com.bookings.padelcenter.application.read.GetAllFieldsUseCase;
import com.bookings.padelcenter.application.read.GetFieldAvailabilityUseCase;
import com.bookings.padelcenter.application.read.GetFieldByIdUseCase;
import com.bookings.padelcenter.application.update.UpdateFieldAvailabilityUseCase;
import com.bookings.padelcenter.application.update.UpdateFieldUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.FieldApiMapper;
import com.bookings.padelcenter.infrastructure.security.AuthenticatedUserResolver;
import com.padelcenter.infrastructure.web.generated.api.FieldsApi;
import com.padelcenter.infrastructure.web.generated.model.FieldAvailability;
import com.padelcenter.infrastructure.web.generated.model.FieldResponse;
import com.padelcenter.infrastructure.web.generated.model.FieldUpdateRequest;
import com.padelcenter.infrastructure.web.generated.model.ListCenterFields200Response;
import com.padelcenter.infrastructure.web.generated.model.UpdateFieldAvailabilityRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FieldController implements FieldsApi {

	private final GetAllFieldsUseCase getAllFieldsUseCase;
	private final GetFieldByIdUseCase getFieldByIdUseCase;
	private final GetFieldAvailabilityUseCase getFieldAvailabilityUseCase;
	private final UpdateFieldAvailabilityUseCase updateFieldAvailabilityUseCase;
	private final UpdateFieldUseCase updateFieldUseCase;
	private final DeleteFieldUseCase deleteFieldUseCase;
	private final FieldApiMapper fieldApiMapper;
	private final AuthenticatedUserResolver authResolver;

	@Override
	public ResponseEntity<ListCenterFields200Response> listFields(Integer page, Integer size, String sort) {
		var query = new GetAllFieldsQuery(page, size);
		var pageResult = getAllFieldsUseCase.execute(query);
		return ResponseEntity.ok(fieldApiMapper.toPagedResponse(pageResult));
	}

	@Override
	public ResponseEntity<FieldResponse> getFieldById(UUID id) {
		var field = getFieldByIdUseCase.execute(new GetFieldByIdQuery(id));
		return ResponseEntity.ok(fieldApiMapper.toResponse(field));
	}

	@Override
	public ResponseEntity<FieldAvailability> getFieldAvailability(UUID fieldId, LocalDate date) {
		var result = getFieldAvailabilityUseCase.execute(new GetFieldAvailabilityQuery(fieldId, date));
		return ResponseEntity.ok(fieldApiMapper.toAvailabilityResponse(result));
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteField(UUID id) {
		var command = new DeleteFieldCommand(id, authResolver.currentUser());
		deleteFieldUseCase.execute(command);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<FieldResponse> updateFieldAvailability(
			UUID fieldId, UpdateFieldAvailabilityRequest updateFieldAvailabilityRequest) {
		var command = new UpdateFieldAvailabilityCommand(fieldId, updateFieldAvailabilityRequest.getAvailable());
		var field = updateFieldAvailabilityUseCase.execute(command);
		return ResponseEntity.ok(fieldApiMapper.toResponse(field));
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<FieldResponse> updateField(UUID id, FieldUpdateRequest fieldUpdateRequest) {
		var command = new UpdateFieldCommand(
				id,
				fieldUpdateRequest.getName(),
				fieldUpdateRequest.getType(),
				java.math.BigDecimal.valueOf(fieldUpdateRequest.getPricePerHour()),
				fieldUpdateRequest.getIsAvailable()
		);
		var field = updateFieldUseCase.execute(command);
		return ResponseEntity.ok(fieldApiMapper.toResponse(field));
	}
}
