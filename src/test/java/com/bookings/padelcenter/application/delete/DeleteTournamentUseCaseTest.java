package com.bookings.padelcenter.application.delete;

import com.bookings.padelcenter.application.command.DeleteTournamentCommand;
import com.bookings.padelcenter.domain.exception.TournamentNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteTournamentUseCase Tests")
class DeleteTournamentUseCaseTest {

	@Mock
	private TournamentRepository tournamentRepository;

	@InjectMocks
	private DeleteTournamentUseCase deleteTournamentUseCase;

	private UUID tournamentId;
	private UUID deletedBy;
	private DeleteTournamentCommand command;
	private Tournament existingTournament;

	@BeforeEach
	void setUp() {
		tournamentId = UUID.randomUUID();
		deletedBy = UUID.randomUUID();
		command = new DeleteTournamentCommand(tournamentId, deletedBy);
		existingTournament = new Tournament(
			tournamentId,
			UUID.randomUUID(),
			"Open Madrid",
			null,
			TournamentFormat.ROUND_ROBIN,
			TournamentStatus.REGISTRATION_OPEN,
			8,
			LocalDate.now(),
			LocalDate.now().plusDays(7),
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("deleteTournament_existingTournament_softDeletes")
	void deleteTournament_existingTournament_softDeletes() {
		when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(existingTournament));
		doNothing().when(tournamentRepository).delete(tournamentId, deletedBy);

		deleteTournamentUseCase.execute(command);

		verify(tournamentRepository).findById(tournamentId);
		verify(tournamentRepository).delete(tournamentId, deletedBy);
	}

	@Test
	@DisplayName("deleteTournament_notFound_throwsTournamentNotFoundException")
	void deleteTournament_notFound_throwsTournamentNotFoundException() {
		when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deleteTournamentUseCase.execute(command))
			.isInstanceOf(TournamentNotFoundException.class);

		verify(tournamentRepository).findById(tournamentId);
		verify(tournamentRepository, never()).delete(any(), any());
	}

	@Test
	@DisplayName("deleteTournament_domainDeleteReturnsCorrectStatus")
	void deleteTournament_domainDeleteReturnsCorrectStatus() {
		var deleted = existingTournament.delete(deletedBy);

		assertThat(deleted.status()).isEqualTo(TournamentStatus.CANCELLED);
		assertThat(deleted.audit().deletedBy()).isEqualTo(deletedBy);
		assertThat(deleted.audit().deletedAt()).isNotNull();
	}
}
