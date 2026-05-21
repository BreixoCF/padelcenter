package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.command.LoginCommand;
import com.bookings.padelcenter.application.create.LoginUseCase;
import com.bookings.padelcenter.application.query.GetCurrentUserQuery;
import com.bookings.padelcenter.application.query.GetMyBookingsQuery;
import com.bookings.padelcenter.application.query.GetMyMatchesQuery;
import com.bookings.padelcenter.application.query.GetMyTournamentsQuery;
import com.bookings.padelcenter.application.read.GetCurrentUserUseCase;
import com.bookings.padelcenter.application.read.GetMyBookingsUseCase;
import com.bookings.padelcenter.application.read.GetMyMatchesUseCase;
import com.bookings.padelcenter.application.read.GetMyTournamentsUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.BookingApiMapper;
import com.bookings.padelcenter.infrastructure.inbound.mapper.TournamentApiMapper;
import com.bookings.padelcenter.infrastructure.inbound.mapper.UserApiMapper;
import com.bookings.padelcenter.infrastructure.security.AuthenticatedUserResolver;
import com.bookings.padelcenter.infrastructure.security.JwtService;
import com.padelcenter.infrastructure.web.generated.api.AuthApi;
import com.padelcenter.infrastructure.web.generated.model.GetMyBookings200Response;
import com.padelcenter.infrastructure.web.generated.model.GetMyMatches200Response;
import com.padelcenter.infrastructure.web.generated.model.GetMyTournaments200Response;
import com.padelcenter.infrastructure.web.generated.model.LoginRequest;
import com.padelcenter.infrastructure.web.generated.model.LoginResponse;
import com.padelcenter.infrastructure.web.generated.model.TokenResponse;
import com.padelcenter.infrastructure.web.generated.model.UserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

	private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

	private final GetMyMatchesUseCase getMyMatchesUseCase;
	private final LoginUseCase loginUseCase;
	private final GetCurrentUserUseCase getCurrentUserUseCase;
	private final GetMyTournamentsUseCase getMyTournamentsUseCase;
	private final GetMyBookingsUseCase getMyBookingsUseCase;
	private final UserApiMapper userApiMapper;
	private final TournamentApiMapper tournamentApiMapper;
	private final BookingApiMapper bookingApiMapper;
	private final JwtService jwtService;
	private final JwtDecoder jwtDecoder;
	private final HttpServletRequest httpRequest;
	private final AuthenticatedUserResolver authResolver;

	@Override
	public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
		var user = loginUseCase.execute(new LoginCommand(loginRequest.getEmail(), loginRequest.getPassword()));
		var accessToken = jwtService.generateAccessToken(user);
		var refreshToken = jwtService.generateRefreshToken(user.userId());

		var cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
			.httpOnly(true)
			.secure(false)
			.path("/api/v1/auth/refresh")
			.maxAge(Duration.ofSeconds(jwtService.refreshTokenExpirySeconds()))
			.sameSite("Strict")
			.build();

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(new LoginResponse()
				.accessToken(accessToken)
				.user(userApiMapper.toResponse(user)));
	}

	@Override
	public ResponseEntity<TokenResponse> refreshToken() {
		var refreshTokenValue = Arrays.stream(
				httpRequest.getCookies() != null ? httpRequest.getCookies() : new Cookie[0])
			.filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
			.findFirst()
			.map(Cookie::getValue)
			.orElseThrow(() -> new com.bookings.padelcenter.domain.exception.UnauthorizedException("No refresh token"));

		var jwt = (Jwt) jwtDecoder.decode(refreshTokenValue);
		var userId = UUID.fromString(jwt.getSubject());
		var user = getCurrentUserUseCase.execute(new GetCurrentUserQuery(userId));
		var newToken = jwtService.generateAccessToken(user);

		return ResponseEntity.ok(new TokenResponse().accessToken(newToken));
	}

	@Override
	public ResponseEntity<Void> logout() {
		var cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
			.httpOnly(true)
			.path("/api/v1/auth/refresh")
			.maxAge(0)
			.build();

		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.build();
	}

	@Override
	public ResponseEntity<UserResponse> getMeProfile() {
		var user = getCurrentUserUseCase.execute(new GetCurrentUserQuery(authResolver.currentUser().userId()));
		return ResponseEntity.ok(userApiMapper.toResponse(user));
	}

	@Override
	public ResponseEntity<GetMyBookings200Response> getMyBookings(Integer page, Integer size, String status) {
		var userId = authResolver.currentUser().userId();
		var query = new GetMyBookingsQuery(userId, status, page != null ? page : 0, size != null ? size : 10);
		var pageResult = getMyBookingsUseCase.execute(query);
		return ResponseEntity.ok(bookingApiMapper.toPagedBookingHistory(pageResult));
	}

	@Override
	public ResponseEntity<GetMyTournaments200Response> getMyTournaments(Integer page, Integer size) {
		var userId = authResolver.currentUser().userId();
		var query = new GetMyTournamentsQuery(userId, page != null ? page : 0, size != null ? size : 20);
		var pageResult = getMyTournamentsUseCase.execute(query);
		return ResponseEntity.ok(tournamentApiMapper.toPagedResponse(pageResult));
	}

	@Override
	public ResponseEntity<GetMyMatches200Response> getMyMatches(Integer page, Integer size) {
		var userId = authResolver.currentUser().userId();
		var query = new GetMyMatchesQuery(userId, page != null ? page : 0, size != null ? size : 20);
		var pageResult = getMyMatchesUseCase.execute(query);
		return ResponseEntity.ok(tournamentApiMapper.toMatchHistoryPagedResponse(pageResult));
	}
}
