package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.ConfirmPairCommand;
import com.bookings.padelcenter.domain.exception.MatchNotFoundException;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.PairStatus;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmPairUseCase Tests")
class ConfirmPairUseCaseTest {

	@Mock
	private TournamentRepository tournamentRepository;

	@InjectMocks
	private ConfirmPairUseCase confirmPairUseCase;

	private UUID tournamentId;
	private UUID pairId;
	private Tournament openTournament;

	@BeforeEach
	void setUp() {
		tournamentId = UUID.randomUUID();
		pairId = UUID.randomUUID();
		openTournament = new Tournament(tournamentId, UUID.randomUUID(), "Copa Test", null,
			TournamentFormat.ROUND_ROBIN, TournamentStatus.REGISTRATION_OPEN, 8,
			LocalDate.now(), LocalDate.now().plusDays(7), Auditable.newAudit());
	}

	@Test
	@DisplayName("confirmPair_pairBelongsToTournament_confirms")
	void confirmPair_pairBelongsToTournament_confirms() {
		var pair = new TournamentPair(pairId, tournamentId, UUID.randomUUID(), UUID.randomUUID(),
			"Pareja 1", PairStatus.PENDING, Instant.now());

		when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(openTournament));
		when(tournamentRepository.findPairById(pairId)).thenReturn(Optional.of(pair));
		when(tournamentRepository.savePair(any(TournamentPair.class))).thenAnswer(inv -> inv.getArgument(0));

		var result = confirmPairUseCase.execute(new ConfirmPairCommand(tournamentId, pairId));

		assertThat(result.status()).isEqualTo(PairStatus.CONFIRMED);
		verify(tournamentRepository).savePair(any(TournamentPair.class));
	}

	@Test
	@DisplayName("confirmPair_pairBelongsToDifferentTournament_throwsMatchNotFoundException")
	void confirmPair_pairBelongsToDifferentTournament_throwsMatchNotFoundException() {
		var otherTournamentId = UUID.randomUUID();
		var pairFromOtherTournament = new TournamentPair(pairId, otherTournamentId, UUID.randomUUID(),
			UUID.randomUUID(), "Pareja Ajena", PairStatus.PENDING, Instant.now());

		when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(openTournament));
		when(tournamentRepository.findPairById(pairId)).thenReturn(Optional.of(pairFromOtherTournament));

		assertThatThrownBy(() -> confirmPairUseCase.execute(new ConfirmPairCommand(tournamentId, pairId)))
			.isInstanceOf(MatchNotFoundException.class)
			.hasMessageContaining(pairId.toString());

		verify(tournamentRepository, never()).savePair(any());
	}

	@Test
	@DisplayName("confirmPair_tournamentNotFound_throwsTournamentNotFoundException")
	void confirmPair_tournamentNotFound_throwsTournamentNotFoundException() {
		when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> confirmPairUseCase.execute(new ConfirmPairCommand(tournamentId, pairId)))
			.isInstanceOf(TournamentNotFoundException.class);

		verify(tournamentRepository, never()).savePair(any());
	}
}
