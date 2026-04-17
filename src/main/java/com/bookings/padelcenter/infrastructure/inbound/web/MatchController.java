package com.bookings.padelcenter.infrastructure.inbound.web;

import com.bookings.padelcenter.application.update.ReportMatchResultUseCase;
import com.bookings.padelcenter.infrastructure.inbound.mapper.TournamentApiMapper;
import com.bookings.padelcenter.infrastructure.security.AuthenticatedUserResolver;
import com.padelcenter.infrastructure.web.generated.api.MatchesApi;
import com.padelcenter.infrastructure.web.generated.model.MatchResponse;
import com.padelcenter.infrastructure.web.generated.model.MatchResultRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MatchController implements MatchesApi {

	private final ReportMatchResultUseCase reportMatchResultUseCase;
	private final TournamentApiMapper tournamentApiMapper;
	private final AuthenticatedUserResolver authResolver;

	@Override
	public ResponseEntity<MatchResponse> reportMatchResult(UUID matchId, MatchResultRequest matchResultRequest) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UUID reportedBy = authResolver.resolveUserId(authentication);
		var command = tournamentApiMapper.toCommand(matchId, reportedBy, matchResultRequest);
		var match = reportMatchResultUseCase.execute(command);
		return ResponseEntity.ok(tournamentApiMapper.toMatchResponse(match));
	}
}
