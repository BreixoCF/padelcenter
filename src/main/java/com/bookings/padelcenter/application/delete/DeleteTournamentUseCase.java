package com.bookings.padelcenter.application.delete;

import com.bookings.padelcenter.application.command.DeleteTournamentCommand;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteTournamentUseCase {

	private final TournamentRepository tournamentRepository;

	@PreAuthorize("@tournamentRoleEvaluator.isAdminOf(@authResolver.resolveUserId(authentication), #command.tournamentId) or hasRole('ADMIN')")
	public void execute(DeleteTournamentCommand command) {
		log.debug("tournament.delete.start tournamentId={} deletedBy={}",
			command.tournamentId(), command.deletedBy());

		var tournament = tournamentRepository.findById(command.tournamentId())
				.orElseThrow(() -> new TournamentNotFoundException(command.tournamentId()));

		tournament.delete(command.deletedBy());

		tournamentRepository.delete(command.tournamentId(), command.deletedBy());

		log.info("tournament.deleted tournamentId={}", command.tournamentId());
	}
}
