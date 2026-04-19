package com.bookings.padelcenter.application.delete;

import com.bookings.padelcenter.application.command.DeleteFieldCommand;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteFieldUseCase Tests")
class DeleteFieldUseCaseTest {

	@Mock
	private FieldRepository fieldRepository;

	@InjectMocks
	private DeleteFieldUseCase deleteFieldUseCase;

	private UUID fieldId;
	private UUID deletedBy;
	private DeleteFieldCommand command;
	private Field existingField;

	@BeforeEach
	void setUp() {
		fieldId = UUID.randomUUID();
		deletedBy = UUID.randomUUID();
		command = new DeleteFieldCommand(fieldId, new AuthenticatedUser(deletedBy));
		existingField = new Field(
			fieldId,
			"Pista 1",
			"INDOOR",
			BigDecimal.valueOf(30.00),
			true,
			null,
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("deleteField_existingField_softDeletes")
	void deleteField_existingField_softDeletes() {
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(existingField));
		when(fieldRepository.save(any(Field.class))).thenAnswer(inv -> inv.getArgument(0));

		var result = deleteFieldUseCase.execute(command);

		assertThat(result.fieldId()).isEqualTo(fieldId);
		assertThat(result.audit().deletedBy()).isEqualTo(deletedBy);
		assertThat(result.audit().deletedAt()).isNotNull();
		verify(fieldRepository).findById(fieldId);
		verify(fieldRepository).save(any(Field.class));
	}

	@Test
	@DisplayName("deleteField_notFound_throwsFieldNotFoundException")
	void deleteField_notFound_throwsFieldNotFoundException() {
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deleteFieldUseCase.execute(command))
			.isInstanceOf(FieldNotFoundException.class)
			.hasMessageContaining(fieldId.toString());

		verify(fieldRepository).findById(fieldId);
		verify(fieldRepository, never()).save(any());
	}

	@Test
	@DisplayName("deleteField_preservesFieldData")
	void deleteField_preservesFieldData() {
		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(existingField));
		when(fieldRepository.save(any(Field.class))).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<Field> captor = ArgumentCaptor.forClass(Field.class);
		deleteFieldUseCase.execute(command);

		verify(fieldRepository).save(captor.capture());
		var saved = captor.getValue();
		assertThat(saved.name()).isEqualTo(existingField.name());
		assertThat(saved.type()).isEqualTo(existingField.type());
		assertThat(saved.audit().deletedBy()).isEqualTo(deletedBy);
	}
}
