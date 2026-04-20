package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.ReportMatchResultCommand;
import com.bookings.padelcenter.domain.exception.InvalidMatchResultException;
import com.bookings.padelcenter.domain.exception.MatchNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.MatchStatus;
import com.bookings.padelcenter.domain.model.PairStatus;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.repository.MatchRepository;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReportMatchResultUseCaseTest {

	@Mock
	private MatchRepository matchRepository;
	@Mock
	private TournamentRepository tournamentRepository;
	@InjectMocks
	private ReportMatchResultUseCase useCase;

	@Test
	@DisplayName("should report match result when caller is a player in the match")
	void execute_validPlayerReportsResult_matchCompletedWithResult() {
		var matchId = UUID.randomUUID();
		var pairAId = UUID.randomUUID();
		var pairBId = UUID.randomUUID();
		var player1OfPairA = UUID.randomUUID();

		var match = buildMatch(matchId, pairAId, pairBId, MatchStatus.SCHEDULED);
		var pairA = buildPair(pairAId, player1OfPairA, UUID.randomUUID());
		var pairB = buildPair(pairBId, UUID.randomUUID(), UUID.randomUUID());
		var completedMatch = match.withResult(
				new com.bookings.padelcenter.domain.model.MatchResult(
						pairAId, 6, 3, player1OfPairA, Instant.now()));

		var command = new ReportMatchResultCommand(matchId, pairAId, 6, 3, player1OfPairA, false);

		given(matchRepository.findById(matchId)).willReturn(Optional.of(match));
		given(tournamentRepository.findPairById(pairAId)).willReturn(Optional.of(pairA));
		given(tournamentRepository.findPairById(pairBId)).willReturn(Optional.of(pairB));
		given(matchRepository.save(any())).willReturn(completedMatch);

		var result = useCase.execute(command);

		assertThat(result.status()).isEqualTo(MatchStatus.COMPLETED);
		assertThat(result.result()).isNotNull();
		assertThat(result.result().winnerPairId()).isEqualTo(pairAId);
		then(matchRepository).should().save(any(Match.class));
	}

	@Test
	@DisplayName("should throw InvalidMatchResultException when caller is not a player in the match")
	void execute_nonPlayerReportsResult_throwsInvalidMatchResultException() {
		var matchId = UUID.randomUUID();
		var pairAId = UUID.randomUUID();
		var pairBId = UUID.randomUUID();
		var outsider = UUID.randomUUID();

		var match = buildMatch(matchId, pairAId, pairBId, MatchStatus.SCHEDULED);
		var pairA = buildPair(pairAId, UUID.randomUUID(), UUID.randomUUID());
		var pairB = buildPair(pairBId, UUID.randomUUID(), UUID.randomUUID());
		var command = new ReportMatchResultCommand(matchId, pairAId, 6, 3, outsider, false);

		given(matchRepository.findById(matchId)).willReturn(Optional.of(match));
		given(tournamentRepository.findPairById(pairAId)).willReturn(Optional.of(pairA));
		given(tournamentRepository.findPairById(pairBId)).willReturn(Optional.of(pairB));

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(InvalidMatchResultException.class);
		then(matchRepository).shouldHaveNoMoreInteractions();
	}

	@Test
	@DisplayName("should throw MatchNotFoundException when match does not exist")
	void execute_matchNotFound_throwsMatchNotFoundException() {
		var matchId = UUID.randomUUID();
		var command = new ReportMatchResultCommand(matchId, UUID.randomUUID(), 6, 3, UUID.randomUUID(), false);

		given(matchRepository.findById(matchId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(MatchNotFoundException.class);
	}

	private Match buildMatch(UUID matchId, UUID pairAId, UUID pairBId, MatchStatus status) {
		return new Match(matchId, UUID.randomUUID(), pairAId, pairBId, 1, null,
				status, null, null, Auditable.newAudit());
	}

	private TournamentPair buildPair(UUID pairId, UUID player1Id, UUID player2Id) {
		return new TournamentPair(pairId, UUID.randomUUID(), player1Id, player2Id,
				PairStatus.CONFIRMED, Instant.now());
	}
}
