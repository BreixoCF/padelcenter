package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.command.SyncUserCommand;
import com.bookings.padelcenter.application.create.SyncUserUseCase;
import com.bookings.padelcenter.application.query.GetCurrentUserQuery;
import com.bookings.padelcenter.application.query.GetMyTournamentsQuery;
import com.bookings.padelcenter.application.read.GetCurrentUserUseCase;
import com.bookings.padelcenter.application.read.GetMyTournamentsUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.TournamentApiMapper;
import com.bookings.padelcenter.infrastructure.inbound.mapper.UserApiMapper;
import com.padelcenter.infrastructure.web.generated.api.AuthApi;
import com.padelcenter.infrastructure.web.generated.model.GetMyTournaments200Response;
import com.padelcenter.infrastructure.web.generated.model.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

	private final SyncUserUseCase syncUserUseCase;
	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final GetMyTournamentsUseCase getMyTournamentsUseCase;
	private final UserApiMapper userApiMapper;
	private final TournamentApiMapper tournamentApiMapper;

	@Override
	public ResponseEntity<UserResponse> syncUser() {
		Jwt jwt = extractJwt();
		var command = new SyncUserCommand(
				jwt.getSubject(),
				jwt.getClaimAsString("email"),
				jwt.getClaimAsString("given_name"),
				jwt.getClaimAsString("family_name")
		);
		var user = syncUserUseCase.execute(command);
		return ResponseEntity.ok(userApiMapper.toResponse(user));
	}

	@Override
	public ResponseEntity<UserResponse> getMeProfile() {
		Jwt jwt = extractJwt();
		var user = getCurrentUserUseCase.execute(new GetCurrentUserQuery(jwt.getSubject()));
		return ResponseEntity.ok(userApiMapper.toResponse(user));
	}

	@Override
	public ResponseEntity<GetMyTournaments200Response> getMyTournaments(Integer page, Integer size) {
		Jwt jwt = extractJwt();
		var user = getCurrentUserUseCase.execute(new GetCurrentUserQuery(jwt.getSubject()));
		var query = new GetMyTournamentsQuery(user.userId(), page != null ? page : 0, size != null ? size : 20);
		var pageResult = getMyTournamentsUseCase.execute(query);
		return ResponseEntity.ok(tournamentApiMapper.toMyTournamentsPagedResponse(pageResult));
	}

	private Jwt extractJwt() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return ((JwtAuthenticationToken) authentication).getToken();
	}
}
