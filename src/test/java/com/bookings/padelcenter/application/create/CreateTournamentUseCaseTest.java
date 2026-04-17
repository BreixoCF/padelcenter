package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateTournamentCommand;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.model.Tournament;
import com.bookings.padelcenter.domain.model.TournamentFormat;
import com.bookings.padelcenter.domain.model.TournamentStatus;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CreateTournamentUseCaseTest {

	@Mock
	private CenterRepository centerRepository;
	@Mock
	private TournamentRepository tournamentRepository;
	@InjectMocks
	private CreateTournamentUseCase useCase;

	@Test
	@DisplayName("should create tournament in DRAFT status when center exists")
	void execute_validCommand_createsDraftTournament() {
		var centerId = UUID.randomUUID();
		var command = new CreateTournamentCommand(
				centerId, "Summer Open", "Annual summer tournament",
				TournamentFormat.ROUND_ROBIN, 8,
				LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3)
		);
		var center = buildCenter(centerId);
		var savedTournament = buildTournament(UUID.randomUUID(), centerId);

		given(centerRepository.findById(centerId)).willReturn(Optional.of(center));
		given(tournamentRepository.save(any())).willReturn(savedTournament);

		var result = useCase.execute(command);

		assertThat(result.centerId()).isEqualTo(centerId);
		assertThat(result.status()).isEqualTo(TournamentStatus.DRAFT);
		then(tournamentRepository).should().save(any(Tournament.class));
	}

	@Test
	@DisplayName("should throw CenterNotFoundException when center does not exist")
	void execute_centerNotFound_throwsCenterNotFoundException() {
		var centerId = UUID.randomUUID();
		var command = new CreateTournamentCommand(
				centerId, "Summer Open", null,
				TournamentFormat.ELIMINATION, 8,
				LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3)
		);

		given(centerRepository.findById(centerId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(command))
				.isInstanceOf(CenterNotFoundException.class);
		then(tournamentRepository).shouldHaveNoInteractions();
	}

	private Center buildCenter(UUID centerId) {
		return new Center(centerId, "Test Center", "Address", "City",
				"000000000", "test@center.com", null, Auditable.newAudit());
	}

	private Tournament buildTournament(UUID tournamentId, UUID centerId) {
		return new Tournament(tournamentId, centerId, "Summer Open",
				"Annual summer tournament", TournamentFormat.ROUND_ROBIN,
				TournamentStatus.DRAFT, 8,
				LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3),
				Auditable.newAudit());
	}
}
