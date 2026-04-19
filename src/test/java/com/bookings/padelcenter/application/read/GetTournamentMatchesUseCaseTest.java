package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetTournamentMatchesQuery;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.MatchStatus;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.repository.MatchRepository;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetTournamentMatchesUseCase Tests")
class GetTournamentMatchesUseCaseTest {

	@Mock
	private TournamentRepository tournamentRepository;

	@Mock
	private MatchRepository matchRepository;

	@InjectMocks
	private GetTournamentMatchesUseCase getTournamentMatchesUseCase;

	@Test
	@DisplayName("getTournamentMatches_paginationParams_delegatesToRepository")
	void getTournamentMatches_paginationParams_delegatesToRepository() {
		var tournamentId = UUID.randomUUID();
		var tournament = new Tournament(tournamentId, UUID.randomUUID(), "Open", null,
				TournamentFormat.ROUND_ROBIN, TournamentStatus.REGISTRATION_OPEN,
				8, LocalDate.now(), LocalDate.now().plusDays(7), null);
		var match = new Match(UUID.randomUUID(), tournamentId, null, null,
				1, "A", MatchStatus.SCHEDULED, null, null, null);
		var expected = new PageResult<>(List.of(match), 0, 20, 1L, 1);

		when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
		when(matchRepository.findByTournamentId(tournamentId, 0, 20)).thenReturn(expected);

		var result = getTournamentMatchesUseCase.execute(new GetTournamentMatchesQuery(tournamentId, 0, 20));

		assertThat(result.content()).hasSize(1);
		assertThat(result.totalElements()).isEqualTo(1L);
		assertThat(result.page()).isZero();
		verify(matchRepository).findByTournamentId(tournamentId, 0, 20);
	}

	@Test
	@DisplayName("getTournamentMatches_tournamentNotFound_throwsTournamentNotFoundException")
	void getTournamentMatches_tournamentNotFound_throwsTournamentNotFoundException() {
		var tournamentId = UUID.randomUUID();
		when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> getTournamentMatchesUseCase.execute(
				new GetTournamentMatchesQuery(tournamentId, 0, 20)))
			.isInstanceOf(TournamentNotFoundException.class);
	}

	@Test
	@DisplayName("getTournamentMatches_emptyPage_returnsEmptyPageResult")
	void getTournamentMatches_emptyPage_returnsEmptyPageResult() {
		var tournamentId = UUID.randomUUID();
		var tournament = new Tournament(tournamentId, UUID.randomUUID(), "Open", null,
				TournamentFormat.ROUND_ROBIN, TournamentStatus.REGISTRATION_OPEN,
				8, LocalDate.now(), LocalDate.now().plusDays(7), null);
		var empty = new PageResult<Match>(List.of(), 0, 20, 0L, 0);

		when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
		when(matchRepository.findByTournamentId(tournamentId, 0, 20)).thenReturn(empty);

		var result = getTournamentMatchesUseCase.execute(new GetTournamentMatchesQuery(tournamentId, 0, 20));

		assertThat(result.content()).isEmpty();
		assertThat(result.totalElements()).isZero();
	}
}
