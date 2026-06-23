package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.CloseRegistrationCommand;
import com.bookings.padelcenter.application.service.BracketGeneratorService;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.InvalidTournamentStatusException;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.PairStatus;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.repository.MatchRepository;
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
public class CloseRegistrationAndGenerateBracketUseCase
		implements CommandUseCase<CloseRegistrationCommand, Tournament> {

	private final TournamentRepository tournamentRepository;
	private final MatchRepository matchRepository;
	private final BracketGeneratorService bracketGeneratorService;

	@NonNull
	@Override
	@PreAuthorize("hasRole('ADMIN') or @tournamentRoleEvaluator.isAdminOf(@authResolver.resolveUserId(authentication), #command.tournamentId)")
	public Tournament execute(CloseRegistrationCommand command) {
		var tournament = tournamentRepository.findById(command.tournamentId())
				.orElseThrow(() -> new TournamentNotFoundException(command.tournamentId()));

		var closed = tournament.closeRegistration();

		var allPairs = tournamentRepository.findPairsByTournamentId(command.tournamentId());
		var confirmedPairs = allPairs.stream()
				.filter(p -> p.status() == PairStatus.CONFIRMED)
				.toList();

		validateMinimumPairs(closed, confirmedPairs.size());

		var matches = bracketGeneratorService.generate(closed, confirmedPairs);
		matchRepository.saveAll(matches);

		var inProgress = closed.startTournament();
		var saved = tournamentRepository.save(inProgress);

		log.info("tournament.bracket.generated tournamentId={} format={} matches={}",
				saved.tournamentId(), saved.format(), matches.size());
		return saved;
	}

	private void validateMinimumPairs(Tournament tournament, int confirmedCount) {
		int min = switch (tournament.format()) {
			case ROUND_ROBIN -> 4;
			case GROUPS_AND_ELIMINATION -> 8;
			case ELIMINATION -> 4;
		};

		if (confirmedCount < min) {
			throw new InvalidTournamentStatusException(
					"Not enough confirmed pairs for " + tournament.format() +
					": required " + min + ", got " + confirmedCount);
		}

		if (tournament.format() == TournamentFormat.ELIMINATION
				&& !bracketGeneratorService.isPowerOfTwo(confirmedCount)) {
			throw new InvalidTournamentStatusException(
					"ELIMINATION format requires a power-of-2 number of pairs, got " + confirmedCount);
		}
	}
}
