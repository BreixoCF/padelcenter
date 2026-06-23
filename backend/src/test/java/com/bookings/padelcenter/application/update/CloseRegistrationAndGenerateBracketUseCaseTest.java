package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.CloseRegistrationCommand;
import com.bookings.padelcenter.application.service.BracketGeneratorService;
import com.bookings.padelcenter.domain.exception.InvalidTournamentStatusException;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.MatchStatus;
import com.bookings.padelcenter.domain.model.PairStatus;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.repository.MatchRepository;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CloseRegistrationAndGenerateBracketUseCaseTest {

	@Mock
	private TournamentRepository tournamentRepository;
	@Mock
	private MatchRepository matchRepository;
	@Mock
	private BracketGeneratorService bracketGeneratorService;
	@InjectMocks
	private CloseRegistrationAndGenerateBracketUseCase useCase;

	@Test
	@DisplayName("should close registration, generate bracket and start tournament")
	void execute_roundRobinWith4Pairs_generatesBracketAndStartsTournament() {
		var tournamentId = UUID.randomUUID();
		var tournament = buildTournament(tournamentId, TournamentStatus.REGISTRATION_OPEN,
				TournamentFormat.ROUND_ROBIN);
		var pairs = buildConfirmedPairs(4, tournamentId);
		var matches = buildMatches(6, tournamentId);
		var inProgressTournament = buildTournament(tournamentId, TournamentStatus.IN_PROGRESS,
				TournamentFormat.ROUND_ROBIN);

		given(tournamentRepository.findById(tournamentId)).willReturn(Optional.of(tournament));
		given(tournamentRepository.findPairsByTournamentId(tournamentId)).willReturn(pairs);
		given(bracketGeneratorService.generate(any(), any())).willReturn(matches);
		given(tournamentRepository.save(any())).willReturn(inProgressTournament);

		var result = useCase.execute(new CloseRegistrationCommand(tournamentId));

		assertThat(result.status()).isEqualTo(TournamentStatus.IN_PROGRESS);
		then(matchRepository).should().saveAll(matches);
		then(tournamentRepository).should().save(any(Tournament.class));
	}

	@Test
	@DisplayName("should throw InvalidTournamentStatusException when fewer than 4 confirmed pairs")
	void execute_insufficientPairs_throwsInvalidTournamentStatusException() {
		var tournamentId = UUID.randomUUID();
		var tournament = buildTournament(tournamentId, TournamentStatus.REGISTRATION_OPEN,
				TournamentFormat.ROUND_ROBIN);
		var pairs = buildConfirmedPairs(2, tournamentId); // only 2 pairs

		given(tournamentRepository.findById(tournamentId)).willReturn(Optional.of(tournament));
		given(tournamentRepository.findPairsByTournamentId(tournamentId)).willReturn(pairs);

		assertThatThrownBy(() -> useCase.execute(new CloseRegistrationCommand(tournamentId)))
				.isInstanceOf(InvalidTournamentStatusException.class);
		then(matchRepository).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("should throw TournamentNotFoundException when tournament does not exist")
	void execute_tournamentNotFound_throwsTournamentNotFoundException() {
		var tournamentId = UUID.randomUUID();

		given(tournamentRepository.findById(tournamentId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new CloseRegistrationCommand(tournamentId)))
				.isInstanceOf(TournamentNotFoundException.class);
	}

	private Tournament buildTournament(UUID id, TournamentStatus status, TournamentFormat format) {
		return new Tournament(id, UUID.randomUUID(), "Test", null, format, status, 16,
				LocalDate.now(), LocalDate.now().plusDays(3), Auditable.newAudit());
	}

	private List<TournamentPair> buildConfirmedPairs(int n, UUID tournamentId) {
		return IntStream.range(0, n)
				.mapToObj(i -> new TournamentPair(UUID.randomUUID(), tournamentId,
						UUID.randomUUID(), UUID.randomUUID(), "", PairStatus.CONFIRMED, Instant.now()))
				.toList();
	}

	private List<Match> buildMatches(int n, UUID tournamentId) {
		return IntStream.range(0, n)
				.mapToObj(i -> new Match(UUID.randomUUID(), tournamentId,
						UUID.randomUUID(), UUID.randomUUID(), 1, null,
						MatchStatus.SCHEDULED, null, null, Auditable.newAudit()))
				.toList();
	}
}
