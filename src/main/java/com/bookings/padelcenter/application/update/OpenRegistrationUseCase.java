package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.OpenRegistrationCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OpenRegistrationUseCase implements CommandUseCase<OpenRegistrationCommand, Tournament> {

	private final TournamentRepository tournamentRepository;

	@NonNull
	@Override
	public Tournament execute(OpenRegistrationCommand command) {
		var tournament = tournamentRepository.findById(command.tournamentId())
				.orElseThrow(() -> new TournamentNotFoundException(command.tournamentId()));

		var updated = tournament.openRegistration();
		var saved = tournamentRepository.save(updated);
		log.info("tournament.registration.opened tournamentId={}", saved.tournamentId());
		return saved;
	}
}
