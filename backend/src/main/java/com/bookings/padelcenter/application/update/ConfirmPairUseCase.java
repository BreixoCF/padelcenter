package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.ConfirmPairCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.InvalidTournamentStatusException;
import com.bookings.padelcenter.domain.exception.MatchNotFoundException;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
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
public class ConfirmPairUseCase implements CommandUseCase<ConfirmPairCommand, TournamentPair> {

	private final TournamentRepository tournamentRepository;

	@NonNull
	@Override
	@PreAuthorize("hasRole('ADMIN') or @tournamentRoleEvaluator.isAdminOf(@authResolver.resolveUserId(authentication), #command.tournamentId)")
	public TournamentPair execute(ConfirmPairCommand command) {
		var tournament = tournamentRepository.findById(command.tournamentId())
				.orElseThrow(() -> new TournamentNotFoundException(command.tournamentId()));

		if (tournament.status() != TournamentStatus.REGISTRATION_OPEN) {
			throw new InvalidTournamentStatusException(
					"Cannot confirm pair — registration is not open for tournament " + command.tournamentId());
		}

		var pair = tournamentRepository.findPairById(command.pairId())
				.filter(p -> p.tournamentId().equals(command.tournamentId()))
				.orElseThrow(() -> new MatchNotFoundException(command.pairId()));

		var confirmed = pair.confirm();
		var saved = tournamentRepository.savePair(confirmed);
		log.info("tournament.pair.confirmed tournamentId={} pairId={}",
				command.tournamentId(), saved.pairId());
		return saved;
	}
}
