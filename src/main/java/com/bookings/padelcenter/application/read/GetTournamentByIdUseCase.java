package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetTournamentByIdQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetTournamentByIdUseCase implements QueryUseCase<GetTournamentByIdQuery, Tournament> {

	private final TournamentRepository tournamentRepository;

	@NonNull
	@Override
	public Tournament execute(GetTournamentByIdQuery query) {
		log.debug("tournament.findById.start tournamentId={}", query.tournamentId());
		return tournamentRepository.findById(query.tournamentId())
				.orElseThrow(() -> new TournamentNotFoundException(query.tournamentId()));
	}
}
