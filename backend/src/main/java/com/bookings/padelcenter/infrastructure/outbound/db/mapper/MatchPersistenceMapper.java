package com.bookings.padelcenter.infrastructure.outbound.db.mapper;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.MatchResult;
import com.bookings.padelcenter.domain.model.MatchStatus;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.MatchEntity;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class MatchPersistenceMapper {

	private static final ZoneId ZONE = ZoneId.of("UTC");

	public MatchEntity toEntity(Match match) {
		var entity = new MatchEntity();
		if (match.matchId() != null) {
			entity.setMatchId(match.matchId());
		}
		entity.setTournamentId(match.tournamentId());
		entity.setPairAId(match.pairAId());
		entity.setPairBId(match.pairBId());
		entity.setRound(match.round());
		entity.setGroupName(match.groupName());
		entity.setStatus(match.status().name());
		if (match.scheduledAt() != null) {
			entity.setScheduledAt(match.scheduledAt().atZone(ZONE).toInstant());
		}
		if (match.result() != null) {
			var r = match.result();
			entity.setWinnerPairId(r.winnerPairId());
			entity.setScoreA(r.scoreA());
			entity.setScoreB(r.scoreB());
			entity.setReportedBy(r.reportedBy());
			entity.setReportedAt(r.reportedAt());
		}
		if (match.audit() != null) {
			entity.setCreatedBy(match.audit().createdBy());
			entity.setCreatedAt(match.audit().createdAt());
			entity.setModifiedBy(match.audit().modifiedBy());
			entity.setModifiedAt(match.audit().modifiedAt());
			entity.setDeletedBy(match.audit().deletedBy());
			entity.setDeletedAt(match.audit().deletedAt());
		}
		return entity;
	}

	public Match toDomain(MatchEntity entity) {
		MatchResult result = null;
		if (entity.getWinnerPairId() != null) {
			result = new MatchResult(
					entity.getWinnerPairId(),
					entity.getScoreA() != null ? entity.getScoreA() : 0,
					entity.getScoreB() != null ? entity.getScoreB() : 0,
					entity.getReportedBy(),
					entity.getReportedAt()
			);
		}
		var scheduledAt = entity.getScheduledAt() != null
				? entity.getScheduledAt().atZone(ZONE).toLocalDateTime()
				: null;
		var audit = new Auditable(
				entity.getCreatedBy(),
				entity.getCreatedAt(),
				entity.getModifiedBy(),
				entity.getModifiedAt(),
				entity.getDeletedBy(),
				entity.getDeletedAt()
		);
		return new Match(
				entity.getMatchId(),
				entity.getTournamentId(),
				entity.getPairAId(),
				entity.getPairBId(),
				entity.getRound(),
				entity.getGroupName(),
				MatchStatus.valueOf(entity.getStatus()),
				scheduledAt,
				result,
				audit
		);
	}
}
