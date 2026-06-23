package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetMyTournamentsQuery;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMyTournamentsUseCase Tests")
class GetMyTournamentsUseCaseTest {

	@Mock
	private TournamentRepository tournamentRepository;

	@InjectMocks
	private GetMyTournamentsUseCase getMyTournamentsUseCase;

	@Test
	@DisplayName("getMyTournaments_userInTournament_returnsTournaments")
	void getMyTournaments_userInTournament_returnsTournaments() {
		var userId = UUID.randomUUID();
		var tournament = new Tournament(
			UUID.randomUUID(), UUID.randomUUID(), "Open Madrid", null,
			TournamentFormat.ROUND_ROBIN, TournamentStatus.REGISTRATION_OPEN,
			8, LocalDate.now(), LocalDate.now().plusDays(7), Auditable.newAudit()
		);
		var expected = new PageResult<>(List.of(tournament), 0, 20, 1L, 1);

		when(tournamentRepository.findByPlayerId(userId, 0, 20)).thenReturn(expected);

		var result = getMyTournamentsUseCase.execute(new GetMyTournamentsQuery(userId, 0, 20));

		assertThat(result.content()).hasSize(1);
		assertThat(result.totalElements()).isEqualTo(1L);
		assertThat(result.page()).isZero();
		verify(tournamentRepository).findByPlayerId(userId, 0, 20);
	}

	@Test
	@DisplayName("getMyTournaments_userNotInAnyTournament_returnsEmpty")
	void getMyTournaments_userNotInAnyTournament_returnsEmpty() {
		var userId = UUID.randomUUID();
		var empty = new PageResult<Tournament>(List.of(), 0, 20, 0L, 0);

		when(tournamentRepository.findByPlayerId(userId, 0, 20)).thenReturn(empty);

		var result = getMyTournamentsUseCase.execute(new GetMyTournamentsQuery(userId, 0, 20));

		assertThat(result.content()).isEmpty();
		assertThat(result.totalElements()).isZero();
		verify(tournamentRepository).findByPlayerId(userId, 0, 20);
	}

	@Test
	@DisplayName("getMyTournaments_delegatesPaginationToRepository")
	void getMyTournaments_delegatesPaginationToRepository() {
		var userId = UUID.randomUUID();
		var page2 = new PageResult<Tournament>(List.of(), 1, 10, 0L, 0);

		when(tournamentRepository.findByPlayerId(userId, 1, 10)).thenReturn(page2);

		var result = getMyTournamentsUseCase.execute(new GetMyTournamentsQuery(userId, 1, 10));

		assertThat(result.page()).isEqualTo(1);
		assertThat(result.size()).isEqualTo(10);
		verify(tournamentRepository).findByPlayerId(userId, 1, 10);
	}
}
