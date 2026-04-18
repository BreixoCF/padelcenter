package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.command.SyncUserCommand;
import com.bookings.padelcenter.application.create.SyncUserUseCase;
import com.bookings.padelcenter.application.query.GetCurrentUserQuery;
import com.bookings.padelcenter.application.read.GetCurrentUserUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.UserApiMapper;
import com.padelcenter.infrastructure.web.generated.api.AuthApi;
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
	private final UserApiMapper userApiMapper;

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

	private Jwt extractJwt() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return ((JwtAuthenticationToken) authentication).getToken();
	}
}
