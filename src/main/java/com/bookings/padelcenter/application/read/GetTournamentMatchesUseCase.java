package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetTournamentMatchesQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.repository.MatchRepository;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetTournamentMatchesUseCase implements QueryUseCase<GetTournamentMatchesQuery, List<Match>> {

	private final TournamentRepository tournamentRepository;
	private final MatchRepository matchRepository;

	@NonNull
	@Override
	public List<Match> execute(GetTournamentMatchesQuery query) {
		tournamentRepository.findById(query.tournamentId())
				.orElseThrow(() -> new TournamentNotFoundException(query.tournamentId()));
		return matchRepository.findByTournamentId(query.tournamentId());
	}
}
