package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.command.CloseRegistrationCommand;
import com.bookings.padelcenter.application.command.ConfirmPairCommand;
import com.bookings.padelcenter.application.command.DeleteTournamentCommand;
import com.bookings.padelcenter.application.command.OpenRegistrationCommand;
import com.bookings.padelcenter.application.command.RegisterPairCommand;
import com.bookings.padelcenter.application.create.CreateTournamentUseCase;
import com.bookings.padelcenter.application.delete.DeleteTournamentUseCase;
import com.bookings.padelcenter.application.query.GetRoundRobinStandingsQuery;
import com.bookings.padelcenter.application.query.GetTournamentByIdQuery;
import com.bookings.padelcenter.application.query.GetTournamentMatchesQuery;
import com.bookings.padelcenter.application.read.GetRoundRobinStandingsUseCase;
import com.bookings.padelcenter.application.read.GetTournamentByIdUseCase;
import com.bookings.padelcenter.application.read.GetTournamentMatchesUseCase;
import com.bookings.padelcenter.application.read.GetTournamentPairsUseCase;
import com.bookings.padelcenter.application.update.CloseRegistrationAndGenerateBracketUseCase;
import com.bookings.padelcenter.application.update.ConfirmPairUseCase;
import com.bookings.padelcenter.application.update.OpenRegistrationUseCase;
import com.bookings.padelcenter.application.update.RegisterPairUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.TournamentApiMapper;
import com.bookings.padelcenter.infrastructure.security.AuthenticatedUserResolver;
import com.padelcenter.infrastructure.web.generated.api.TournamentsApi;
import com.padelcenter.infrastructure.web.generated.model.GetTournamentMatches200Response;
import com.padelcenter.infrastructure.web.generated.model.RoundRobinStanding;
import com.padelcenter.infrastructure.web.generated.model.RegisterPairRequest;
import com.padelcenter.infrastructure.web.generated.model.TournamentPairResponse;
import com.padelcenter.infrastructure.web.generated.model.TournamentRequest;
import com.padelcenter.infrastructure.web.generated.model.TournamentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TournamentController implements TournamentsApi {

	private final GetRoundRobinStandingsUseCase getRoundRobinStandingsUseCase;
	private final CreateTournamentUseCase createTournamentUseCase;
	private final GetTournamentByIdUseCase getTournamentByIdUseCase;
	private final DeleteTournamentUseCase deleteTournamentUseCase;
	private final GetTournamentMatchesUseCase getTournamentMatchesUseCase;
	private final GetTournamentPairsUseCase getTournamentPairsUseCase;
	private final OpenRegistrationUseCase openRegistrationUseCase;
	private final CloseRegistrationAndGenerateBracketUseCase closeRegistrationUseCase;
	private final RegisterPairUseCase registerPairUseCase;
	private final ConfirmPairUseCase confirmPairUseCase;
	private final TournamentApiMapper tournamentApiMapper;
	private final AuthenticatedUserResolver authResolver;

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<TournamentResponse> createTournament(TournamentRequest tournamentRequest) {
		var command = tournamentApiMapper.toCommand(tournamentRequest);
		var tournament = createTournamentUseCase.execute(command);
		return ResponseEntity.status(HttpStatus.CREATED).body(tournamentApiMapper.toResponse(tournament));
	}

	@Override
	@PreAuthorize("@tournamentRoleEvaluator.isAdminOf(@authResolver.resolveUserId(authentication), #tournamentId)"
			+ " or hasRole('ADMIN')")
	public ResponseEntity<Void> deleteTournament(UUID tournamentId) {
		var command = new DeleteTournamentCommand(tournamentId, authResolver.currentUser().userId());
		deleteTournamentUseCase.execute(command);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<TournamentResponse> getTournamentById(UUID tournamentId) {
		var tournament = getTournamentByIdUseCase.execute(new GetTournamentByIdQuery(tournamentId));
		return ResponseEntity.ok(tournamentApiMapper.toResponse(tournament));
	}

	@Override
	@PreAuthorize("hasRole('ADMIN') or "
			+ "@tournamentRoleEvaluator.isAdminOf(@authResolver.resolveUserId(authentication), #tournamentId)")
	public ResponseEntity<TournamentResponse> openRegistration(UUID tournamentId) {
		var tournament = openRegistrationUseCase.execute(new OpenRegistrationCommand(tournamentId));
		return ResponseEntity.ok(tournamentApiMapper.toResponse(tournament));
	}

	@Override
	@PreAuthorize("hasRole('ADMIN') or "
			+ "@tournamentRoleEvaluator.isAdminOf(@authResolver.resolveUserId(authentication), #tournamentId)")
	public ResponseEntity<TournamentResponse> closeRegistration(UUID tournamentId) {
		var tournament = closeRegistrationUseCase.execute(new CloseRegistrationCommand(tournamentId));
		return ResponseEntity.ok(tournamentApiMapper.toResponse(tournament));
	}

	@Override
	public ResponseEntity<TournamentPairResponse> registerPair(UUID tournamentId, RegisterPairRequest registerPairRequest) {
		var command = new RegisterPairCommand(
				tournamentId,
				registerPairRequest.getPlayer1Id(),
				registerPairRequest.getPlayer2Id(),
				registerPairRequest.getTeamName()
		);
		var pair = registerPairUseCase.execute(command);
		return ResponseEntity.status(HttpStatus.CREATED).body(tournamentApiMapper.toPairResponse(pair));
	}

	@Override
	public ResponseEntity<List<TournamentPairResponse>> getTournamentPairs(UUID tournamentId) {
		var pairs = getTournamentPairsUseCase.execute(tournamentId);
		return ResponseEntity.ok(tournamentApiMapper.toPairResponseList(pairs));
	}

	@Override
	@PreAuthorize("hasRole('ADMIN') or "
			+ "@tournamentRoleEvaluator.isAdminOf(@authResolver.resolveUserId(authentication), #tournamentId)")
	public ResponseEntity<TournamentPairResponse> confirmPair(UUID tournamentId, UUID pairId) {
		var pair = confirmPairUseCase.execute(new ConfirmPairCommand(tournamentId, pairId));
		return ResponseEntity.ok(tournamentApiMapper.toPairResponse(pair));
	}

	@Override
	public ResponseEntity<List<RoundRobinStanding>> getTournamentStandings(UUID tournamentId) {
		var standings = getRoundRobinStandingsUseCase.execute(new GetRoundRobinStandingsQuery(tournamentId));
		return ResponseEntity.ok(standings.stream().map(tournamentApiMapper::toStandingResponse).toList());
	}

	@Override
	public ResponseEntity<GetTournamentMatches200Response> getTournamentMatches(
			UUID tournamentId, Integer page, Integer size) {
		var query = new GetTournamentMatchesQuery(
				tournamentId,
				page != null ? page : 0,
				size != null ? size : 20
		);
		var pageResult = getTournamentMatchesUseCase.execute(query);
		return ResponseEntity.ok(tournamentApiMapper.toMatchPagedResponse(pageResult));
	}
}
