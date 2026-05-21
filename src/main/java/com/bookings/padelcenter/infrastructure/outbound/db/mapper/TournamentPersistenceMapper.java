package com.bookings.padelcenter.infrastructure.outbound.db.mapper;

import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.PairStatus;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.TournamentEntity;
import com.bookings.padelcenter.infrastructure.outbound.db.entity.TournamentPairEntity;
import org.springframework.stereotype.Component;

@Component
public class TournamentPersistenceMapper {

	public TournamentEntity toEntity(Tournament tournament) {
		var entity = new TournamentEntity();
		if (tournament.tournamentId() != null) {
			entity.setTournamentId(tournament.tournamentId());
		}
		entity.setCenterId(tournament.centerId());
		entity.setName(tournament.name());
		entity.setDescription(tournament.description());
		entity.setFormat(tournament.format().name());
		entity.setStatus(tournament.status().name());
		entity.setMaxPairs(tournament.maxPairs());
		entity.setStartDate(tournament.startDate());
		entity.setEndDate(tournament.endDate());
		if (tournament.audit() != null) {
			entity.setCreatedBy(tournament.audit().createdBy());
			entity.setCreatedAt(tournament.audit().createdAt());
			entity.setModifiedBy(tournament.audit().modifiedBy());
			entity.setModifiedAt(tournament.audit().modifiedAt());
			entity.setDeletedBy(tournament.audit().deletedBy());
			entity.setDeletedAt(tournament.audit().deletedAt());
		}
		return entity;
	}

	public Tournament toDomain(TournamentEntity entity) {
		var audit = new Auditable(
				entity.getCreatedBy(),
				entity.getCreatedAt(),
				entity.getModifiedBy(),
				entity.getModifiedAt(),
				entity.getDeletedBy(),
				entity.getDeletedAt()
		);
		return new Tournament(
				entity.getTournamentId(),
				entity.getCenterId(),
				entity.getName(),
				entity.getDescription(),
				TournamentFormat.valueOf(entity.getFormat()),
				TournamentStatus.valueOf(entity.getStatus()),
				entity.getMaxPairs(),
				entity.getStartDate(),
				entity.getEndDate(),
				audit
		);
	}

	public TournamentPairEntity toPairEntity(TournamentPair pair) {
		var entity = new TournamentPairEntity();
		if (pair.pairId() != null) {
			entity.setPairId(pair.pairId());
		}
		entity.setTournamentId(pair.tournamentId());
		entity.setPlayer1Id(pair.player1Id());
		entity.setPlayer2Id(pair.player2Id());
		entity.setTeamName(pair.teamName() != null ? pair.teamName() : "");
		entity.setStatus(pair.status().name());
		entity.setRegisteredAt(pair.registeredAt());
		return entity;
	}

	public TournamentPair toPairDomain(TournamentPairEntity entity) {
		return new TournamentPair(
				entity.getPairId(),
				entity.getTournamentId(),
				entity.getPlayer1Id(),
				entity.getPlayer2Id(),
				entity.getTeamName(),
				PairStatus.valueOf(entity.getStatus()),
				entity.getRegisteredAt()
		);
	}
}
