package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetTournamentPairsUseCase {

	private final TournamentRepository tournamentRepository;

	@Transactional(readOnly = true)
	public List<TournamentPair> execute(UUID tournamentId) {
		tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new TournamentNotFoundException(tournamentId));
		return tournamentRepository.findPairsByTournamentId(tournamentId);
	}
}
