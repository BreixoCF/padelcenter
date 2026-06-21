package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateTournamentCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CreateTournamentUseCase implements CommandUseCase<CreateTournamentCommand, Tournament> {

	private final CenterRepository centerRepository;
	private final TournamentRepository tournamentRepository;

	@NonNull
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public Tournament execute(CreateTournamentCommand command) {
		centerRepository.findById(command.centerId())
				.orElseThrow(() -> new CenterNotFoundException(command.centerId()));

		var tournament = new Tournament(
				UuidCreator.getTimeOrderedEpoch(),
				command.centerId(),
				command.name(),
				command.description(),
				command.format(),
				TournamentStatus.DRAFT,
				command.maxPairs(),
				command.startDate(),
				command.endDate(),
				Auditable.newAudit()
		);

		var saved = tournamentRepository.save(tournament);
		log.info("tournament.created tournamentId={} centerId={} format={}",
				saved.tournamentId(), saved.centerId(), saved.format());
		return saved;
	}
}
