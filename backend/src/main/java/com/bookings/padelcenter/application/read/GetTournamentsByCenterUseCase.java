package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetTournamentsByCenterQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.repository.CenterRepository;
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
public class GetTournamentsByCenterUseCase implements QueryUseCase<GetTournamentsByCenterQuery, PageResult<Tournament>> {

	private final CenterRepository centerRepository;
	private final TournamentRepository tournamentRepository;

	@NonNull
	@Override
	public PageResult<Tournament> execute(GetTournamentsByCenterQuery query) {
		centerRepository.findById(query.centerId())
				.orElseThrow(() -> new CenterNotFoundException(query.centerId()));
		return tournamentRepository.findByCenterId(query.centerId(), query.page(), query.size());
	}
}
