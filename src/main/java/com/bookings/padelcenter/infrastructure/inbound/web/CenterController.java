package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.command.DeleteCenterCommand;
import com.bookings.padelcenter.application.create.CreateCenterUseCase;
import com.bookings.padelcenter.application.create.CreateFieldUseCase;
import com.bookings.padelcenter.application.delete.DeleteCenterUseCase;
import com.bookings.padelcenter.application.query.GetAllCentersQuery;
import com.bookings.padelcenter.application.query.GetCenterBookingsQuery;
import com.bookings.padelcenter.application.query.GetCenterByIdQuery;
import com.bookings.padelcenter.application.query.GetFieldsByCenterQuery;
import com.bookings.padelcenter.application.query.GetTournamentsByCenterQuery;
import com.bookings.padelcenter.application.read.GetAllCentersUseCase;
import com.bookings.padelcenter.application.read.GetCenterBookingsUseCase;
import com.bookings.padelcenter.application.read.GetCenterByIdUseCase;
import com.bookings.padelcenter.application.read.GetFieldsByCenterUseCase;
import com.bookings.padelcenter.application.read.GetTournamentsByCenterUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.BookingApiMapper;
import com.bookings.padelcenter.infrastructure.inbound.mapper.CenterApiMapper;
import com.bookings.padelcenter.infrastructure.inbound.mapper.FieldApiMapper;
import com.bookings.padelcenter.infrastructure.inbound.mapper.TournamentApiMapper;
import com.bookings.padelcenter.infrastructure.security.AuthenticatedUserResolver;
import com.padelcenter.infrastructure.web.generated.api.CentersApi;
import com.padelcenter.infrastructure.web.generated.model.CenterRequest;
import com.padelcenter.infrastructure.web.generated.model.CenterResponse;
import com.padelcenter.infrastructure.web.generated.model.FieldRequest;
import com.padelcenter.infrastructure.web.generated.model.FieldResponse;
import com.padelcenter.infrastructure.web.generated.model.GetCenterBookings200Response;
import com.padelcenter.infrastructure.web.generated.model.GetMyTournaments200Response;
import com.padelcenter.infrastructure.web.generated.model.ListCenterFields200Response;
import com.padelcenter.infrastructure.web.generated.model.ListCenters200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CenterController implements CentersApi {

	private final CreateCenterUseCase createCenterUseCase;
	private final GetAllCentersUseCase getAllCentersUseCase;
	private final GetCenterByIdUseCase getCenterByIdUseCase;
	private final DeleteCenterUseCase deleteCenterUseCase;
	private final CreateFieldUseCase createFieldUseCase;
	private final GetFieldsByCenterUseCase getFieldsByCenterUseCase;
	private final GetCenterBookingsUseCase getCenterBookingsUseCase;
	private final GetTournamentsByCenterUseCase getTournamentsByCenterUseCase;
	private final CenterApiMapper centerApiMapper;
	private final FieldApiMapper fieldApiMapper;
	private final BookingApiMapper bookingApiMapper;
	private final TournamentApiMapper tournamentApiMapper;
	private final AuthenticatedUserResolver authResolver;

	@Override
	@PreAuthorize("hasRole('ADMIN')")
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
	public ResponseEntity<ListCenterFields200Response> listCenterFields(
			UUID centerId, String type, Boolean available, Integer page, Integer size) {
		var query = new GetFieldsByCenterQuery(centerId, type, available, page, size);
		var pageResult = getFieldsByCenterUseCase.execute(query);
		return ResponseEntity.ok(fieldApiMapper.toPagedResponse(pageResult));
	}

	@Override
	public ResponseEntity<CenterResponse> getCenterById(UUID centerId) {
		var center = getCenterByIdUseCase.execute(new GetCenterByIdQuery(centerId));
		return ResponseEntity.ok(centerApiMapper.toResponse(center));
	}

	@Override
	@PreAuthorize("hasRole('ADMIN') or "
			+ "@centerRoleEvaluator.isAdminOf(@authResolver.resolveUserId(authentication), #centerId)")
	public ResponseEntity<GetCenterBookings200Response> getCenterBookings(
			UUID centerId, LocalDate date, LocalDate startDate, LocalDate endDate,
			UUID fieldId, String status, Integer page, Integer size) {
		var query = new GetCenterBookingsQuery(centerId, fieldId, date, startDate, endDate, status,
				page != null ? page : 0, size != null ? size : 20);
		var pageResult = getCenterBookingsUseCase.execute(query);
		return ResponseEntity.ok(bookingApiMapper.toPagedCenterBookings(pageResult));
	}

	@Override
	public ResponseEntity<GetMyTournaments200Response> getTournamentsByCenter(
			UUID centerId, Integer page, Integer size) {
		var query = new GetTournamentsByCenterQuery(centerId, page != null ? page : 0, size != null ? size : 20);
		var pageResult = getTournamentsByCenterUseCase.execute(query);
		return ResponseEntity.ok(tournamentApiMapper.toPagedResponse(pageResult));
	}

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteCenter(UUID centerId) {
		var command = new DeleteCenterCommand(centerId, authResolver.currentUser());
		deleteCenterUseCase.execute(command);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasRole('ADMIN') or @centerRoleEvaluator.isAdminOf("
			+ "@authResolver.resolveUserId(authentication), #centerId)")
	public ResponseEntity<FieldResponse> createField(UUID centerId, FieldRequest fieldRequest) {
		var command = fieldApiMapper.toCommand(centerId, fieldRequest);
		var field = createFieldUseCase.execute(command);
		return ResponseEntity.status(HttpStatus.CREATED).body(fieldApiMapper.toResponse(field));
	}
}
