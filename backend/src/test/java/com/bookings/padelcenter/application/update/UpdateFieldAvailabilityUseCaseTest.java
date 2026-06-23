package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateFieldAvailabilityCommand;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateFieldAvailabilityUseCase")
class UpdateFieldAvailabilityUseCaseTest {

	@Mock FieldRepository fieldRepository;
	@InjectMocks UpdateFieldAvailabilityUseCase useCase;

	private static final UUID FIELD_ID = UUID.randomUUID();
	private final Center center = new Center(UUID.randomUUID(), "Center", "Addr", "City",
			null, null, null, Auditable.newAudit());
	private final Field availableField = new Field(FIELD_ID, "Pista 1", "Indoor",
			BigDecimal.valueOf(25), true, center, Auditable.newAudit());

	@Test
	@DisplayName("should mark field as unavailable when available=false")
	void updateAvailability_setFalse_fieldMarkedUnavailable() {
		given(fieldRepository.findById(FIELD_ID)).willReturn(Optional.of(availableField));
		given(fieldRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

		var result = useCase.execute(new UpdateFieldAvailabilityCommand(FIELD_ID, false));

		assertThat(result.isAvailable()).isFalse();
		assertThat(result.fieldId()).isEqualTo(FIELD_ID);
		assertThat(result.name()).isEqualTo("Pista 1");
	}

	@Test
	@DisplayName("should mark field as available when available=true")
	void updateAvailability_setTrue_fieldMarkedAvailable() {
		var unavailableField = new Field(FIELD_ID, "Pista 1", "Indoor",
				BigDecimal.valueOf(25), false, center, Auditable.newAudit());
		given(fieldRepository.findById(FIELD_ID)).willReturn(Optional.of(unavailableField));
		given(fieldRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

		var result = useCase.execute(new UpdateFieldAvailabilityCommand(FIELD_ID, true));

		assertThat(result.isAvailable()).isTrue();
	}

	@Test
	@DisplayName("should throw FieldNotFoundException when field does not exist")
	void updateAvailability_fieldNotFound_throwsFieldNotFoundException() {
		given(fieldRepository.findById(FIELD_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new UpdateFieldAvailabilityCommand(FIELD_ID, false)))
				.isInstanceOf(FieldNotFoundException.class);
	}
}
