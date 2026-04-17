package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.RegisterPairCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.InvalidTournamentStatusException;
import com.bookings.padelcenter.domain.exception.PlayerAlreadyInPairException;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.exception.UserNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.PairStatus;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import com.bookings.padelcenter.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterPairUseCase implements CommandUseCase<RegisterPairCommand, TournamentPair> {

	private final TournamentRepository tournamentRepository;
	private final UserRepository userRepository;

	@NonNull
	@Override
	public TournamentPair execute(RegisterPairCommand command) {
		var tournament = tournamentRepository.findById(command.tournamentId())
				.orElseThrow(() -> new TournamentNotFoundException(command.tournamentId()));

		if (tournament.status() != TournamentStatus.REGISTRATION_OPEN) {
			throw new InvalidTournamentStatusException(
					"Registration is not open for tournament " + command.tournamentId());
		}

		userRepository.findById(command.player1Id())
				.orElseThrow(() -> new UserNotFoundException(command.player1Id()));
		userRepository.findById(command.player2Id())
				.orElseThrow(() -> new UserNotFoundException(command.player2Id()));

		if (tournamentRepository.existsPairWithPlayer(command.tournamentId(), command.player1Id())) {
			throw new PlayerAlreadyInPairException(command.player1Id());
		}
		if (tournamentRepository.existsPairWithPlayer(command.tournamentId(), command.player2Id())) {
			throw new PlayerAlreadyInPairException(command.player2Id());
		}

		var pair = new TournamentPair(null, command.tournamentId(),
				command.player1Id(), command.player2Id(), PairStatus.PENDING, Instant.now());

		var saved = tournamentRepository.savePair(pair);
		log.info("tournament.pair.registered tournamentId={} pairId={}",
				command.tournamentId(), saved.pairId());
		return saved;
	}
}
