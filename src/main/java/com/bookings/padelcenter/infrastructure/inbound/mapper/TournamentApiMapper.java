package com.bookings.padelcenter.infrastructure.inbound.mapper;

import com.bookings.padelcenter.application.command.CreateTournamentCommand;
import com.bookings.padelcenter.application.command.ReportMatchResultCommand;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.padelcenter.infrastructure.web.generated.model.AuditableResponse;
import com.padelcenter.infrastructure.web.generated.model.GetMyTournaments200Response;
import com.padelcenter.infrastructure.web.generated.model.GetTournamentMatches200Response;
import com.padelcenter.infrastructure.web.generated.model.MatchResponse;
import com.padelcenter.infrastructure.web.generated.model.MatchResultRequest;
import com.padelcenter.infrastructure.web.generated.model.MatchResultResponse;
import com.padelcenter.infrastructure.web.generated.model.TournamentPairResponse;
import com.padelcenter.infrastructure.web.generated.model.TournamentRequest;
import com.padelcenter.infrastructure.web.generated.model.TournamentResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
public class TournamentApiMapper {

	public CreateTournamentCommand toCommand(TournamentRequest request) {
		return new CreateTournamentCommand(
				request.getCenterId(),
				request.getName(),
				request.getDescription(),
				TournamentFormat.valueOf(request.getFormat().getValue()),
				request.getMaxPairs(),
				request.getStartDate(),
				request.getEndDate()
		);
	}

	public ReportMatchResultCommand toCommand(UUID matchId, UUID reportedBy, boolean isAdmin, MatchResultRequest request) {
		return new ReportMatchResultCommand(
				matchId,
				request.getWinnerPairId(),
				request.getScoreA(),
				request.getScoreB(),
				reportedBy,
				isAdmin
		);
	}

	public TournamentResponse toResponse(Tournament tournament) {
		var response = new TournamentResponse();
		response.setTournamentId(tournament.tournamentId());
		response.setCenterId(tournament.centerId());
		response.setName(tournament.name());
		response.setDescription(tournament.description());
		response.setFormat(TournamentResponse.FormatEnum.fromValue(tournament.format().name()));
		response.setStatus(TournamentResponse.StatusEnum.fromValue(tournament.status().name()));
		response.setMaxPairs(tournament.maxPairs());
		response.setStartDate(tournament.startDate());
		response.setEndDate(tournament.endDate());
		if (tournament.audit() != null) {
			var audit = new AuditableResponse();
			audit.setCreatedAt(tournament.audit().createdAt().atOffset(ZoneOffset.UTC));
			audit.setModifiedAt(tournament.audit().modifiedAt().atOffset(ZoneOffset.UTC));
			response.setAudit(audit);
		}
		return response;
	}

	public TournamentPairResponse toPairResponse(TournamentPair pair) {
		var response = new TournamentPairResponse();
		response.setPairId(pair.pairId());
		response.setTournamentId(pair.tournamentId());
		response.setPlayer1Id(pair.player1Id());
		response.setPlayer2Id(pair.player2Id());
		response.setStatus(TournamentPairResponse.StatusEnum.fromValue(pair.status().name()));
		if (pair.registeredAt() != null) {
			response.setRegisteredAt(pair.registeredAt().atOffset(ZoneOffset.UTC));
		}
		return response;
	}

	public MatchResponse toMatchResponse(Match match) {
		var response = new MatchResponse();
		response.setMatchId(match.matchId());
		response.setTournamentId(match.tournamentId());
		response.setPairAId(match.pairAId());
		response.setPairBId(match.pairBId());
		response.setRound(match.round());
		response.setGroupName(match.groupName());
		response.setStatus(MatchResponse.StatusEnum.fromValue(match.status().name()));
		if (match.scheduledAt() != null) {
			response.setScheduledAt(match.scheduledAt().atOffset(ZoneOffset.UTC));
		}
		if (match.result() != null) {
			var result = new MatchResultResponse();
			result.setWinnerPairId(match.result().winnerPairId());
			result.setScoreA(match.result().scoreA());
			result.setScoreB(match.result().scoreB());
			result.setReportedBy(match.result().reportedBy());
			result.setReportedAt(match.result().reportedAt().atOffset(ZoneOffset.UTC));
			response.setResult(result);
		}
		return response;
	}

	public GetMyTournaments200Response toPagedResponse(PageResult<Tournament> pageResult) {
		var response = new GetMyTournaments200Response();
		response.setContent(pageResult.content().stream().map(this::toResponse).toList());
		response.setTotalElements(pageResult.totalElements());
		response.setTotalPages(pageResult.totalPages());
		response.setCurrentPage(pageResult.page());
		response.setPageSize(pageResult.size());
		return response;
	}

	public List<TournamentPairResponse> toPairResponseList(List<TournamentPair> pairs) {
		return pairs.stream().map(this::toPairResponse).toList();
	}

	public GetMyTournaments200Response toMyTournamentsPagedResponse(PageResult<Tournament> pageResult) {
		var response = new GetMyTournaments200Response();
		response.setContent(pageResult.content().stream().map(this::toResponse).toList());
		response.setTotalElements(pageResult.totalElements());
		response.setTotalPages(pageResult.totalPages());
		response.setCurrentPage(pageResult.page());
		response.setPageSize(pageResult.size());
		return response;
	}

	public GetTournamentMatches200Response toMatchPagedResponse(PageResult<Match> pageResult) {
		var response = new GetTournamentMatches200Response();
		response.setContent(pageResult.content().stream().map(this::toMatchResponse).toList());
		response.setTotalElements(pageResult.totalElements());
		response.setTotalPages(pageResult.totalPages());
		response.setCurrentPage(pageResult.page());
		response.setPageSize(pageResult.size());
		return response;
	}

	public List<MatchResponse> toMatchResponseList(List<Match> matches) {
		return matches.stream().map(this::toMatchResponse).toList();
	}
}
