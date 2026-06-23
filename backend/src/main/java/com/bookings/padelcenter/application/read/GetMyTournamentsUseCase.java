package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetMyTournamentsQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.model.PageResult;
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
public class GetMyTournamentsUseCase implements QueryUseCase<GetMyTournamentsQuery, PageResult<Tournament>> {

	private final TournamentRepository tournamentRepository;

	@NonNull
	@Override
	public PageResult<Tournament> execute(GetMyTournamentsQuery query) {
		log.debug("tournament.my.list.start userId={} page={} size={}",
			query.userId(), query.page(), query.size());

		var result = tournamentRepository.findByPlayerId(query.userId(), query.page(), query.size());

		log.debug("tournament.my.list.found count={} totalPages={} userId={}",
			result.content().size(), result.totalPages(), query.userId());

		return result;
	}
}
