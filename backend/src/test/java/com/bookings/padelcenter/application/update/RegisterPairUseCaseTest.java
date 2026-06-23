package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.RegisterPairCommand;
import com.bookings.padelcenter.domain.exception.InvalidTournamentStatusException;
import com.bookings.padelcenter.domain.exception.PlayerAlreadyInPairException;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.PairStatus;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.model.User;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import com.bookings.padelcenter.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RegisterPairUseCaseTest {

	@Mock
	private TournamentRepository tournamentRepository;
	@Mock
	private UserRepository userRepository;
	@InjectMocks
	private RegisterPairUseCase useCase;

	@Test
	@DisplayName("should register pair in PENDING status when tournament is open")
	void execute_openTournament_registersPairAsPending() {
		var tournamentId = UUID.randomUUID();
		var player1Id = UUID.randomUUID();
		var player2Id = UUID.randomUUID();
		var command = new RegisterPairCommand(tournamentId, player1Id, player2Id, "Team A");

		var tournament = buildTournament(tournamentId, TournamentStatus.REGISTRATION_OPEN);
		var savedPair = new TournamentPair(UUID.randomUUID(), tournamentId,
				player1Id, player2Id, "", PairStatus.PENDING, Instant.now());

		given(tournamentRepository.findById(tournamentId)).willReturn(Optional.of(tournament));
		given(userRepository.findById(player1Id)).willReturn(Optional.of(buildUser(player1Id)));
		given(userRepository.findById(player2Id)).willReturn(Optional.of(buildUser(player2Id)));
		given(tournamentRepository.existsPairWithPlayer(tournamentId, player1Id)).willReturn(false);
		given(tournamentRepository.existsPairWithPlayer(tournamentId, player2Id)).willReturn(false);
		given(tournamentRepository.savePair(any())).willReturn(savedPair);

		var result = useCase.execute(command);

		assertThat(result.status()).isEqualTo(PairStatus.PENDING);
		assertThat(result.tournamentId()).isEqualTo(tournamentId);
		then(tournamentRepository).should().savePair(any(TournamentPair.class));
	}

	@Test
	@DisplayName("should throw InvalidTournamentStatusException when tournament is not open")
	void execute_tournamentNotOpen_throwsInvalidTournamentStatusException() {
		var tournamentId = UUID.randomUUID();
		var command = new RegisterPairCommand(tournamentId, UUID.randomUUID(), UUID.randomUUID(), "");
		var tournament = buildTournament(tournamentId, TournamentStatus.DRAFT);

		given(tournamentRepository.findById(tournamentId)).willReturn(Optional.of(tournament));

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(InvalidTournamentStatusException.class);
		then(tournamentRepository).should().findById(tournamentId);
		then(tournamentRepository).shouldHaveNoMoreInteractions();
	}

	@Test
	@DisplayName("should throw PlayerAlreadyInPairException when player1 is already in a pair")
	void execute_player1AlreadyRegistered_throwsPlayerAlreadyInPairException() {
		var tournamentId = UUID.randomUUID();
		var player1Id = UUID.randomUUID();
		var player2Id = UUID.randomUUID();
		var command = new RegisterPairCommand(tournamentId, player1Id, player2Id, "");
		var tournament = buildTournament(tournamentId, TournamentStatus.REGISTRATION_OPEN);

		given(tournamentRepository.findById(tournamentId)).willReturn(Optional.of(tournament));
		given(userRepository.findById(player1Id)).willReturn(Optional.of(buildUser(player1Id)));
		given(userRepository.findById(player2Id)).willReturn(Optional.of(buildUser(player2Id)));
		given(tournamentRepository.existsPairWithPlayer(tournamentId, player1Id)).willReturn(true);

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(PlayerAlreadyInPairException.class);
		then(tournamentRepository).shouldHaveNoMoreInteractions();
	}

	@Test
	@DisplayName("should throw TournamentNotFoundException when tournament does not exist")
	void execute_tournamentNotFound_throwsTournamentNotFoundException() {
		var tournamentId = UUID.randomUUID();
		var command = new RegisterPairCommand(tournamentId, UUID.randomUUID(), UUID.randomUUID(), "");

		given(tournamentRepository.findById(tournamentId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(TournamentNotFoundException.class);
	}

	private Tournament buildTournament(UUID tournamentId, TournamentStatus status) {
		return new Tournament(tournamentId, UUID.randomUUID(), "Test Tournament", null,
				TournamentFormat.ROUND_ROBIN, status, 8,
				LocalDate.now(), LocalDate.now().plusDays(3), Auditable.newAudit());
	}

	private User buildUser(UUID userId) {
		return new User(userId, "First", "Last", "user@test.com",
				"hash", "000000000", Set.of(), Auditable.newAudit());
	}
}
