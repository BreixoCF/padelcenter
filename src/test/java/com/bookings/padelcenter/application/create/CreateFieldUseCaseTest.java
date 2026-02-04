package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateFieldCommand;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.repository.CenterRepository;
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
@DisplayName("CreateFieldUseCase Tests")
class CreateFieldUseCaseTest {

	@Mock
	private CenterRepository centerRepository;

	@Mock
	private FieldRepository fieldRepository;

	@InjectMocks
	private CreateFieldUseCase createFieldUseCase;

	private UUID centerId;
	private CreateFieldCommand command;
	private Center existingCenter;
	private Field expectedField;

	@BeforeEach
	void setUp() {
		centerId = UUID.randomUUID();

		command = new CreateFieldCommand(
			"Court 1",
			"Indoor",
			new BigDecimal("25.00"),
			true,
			centerId
		);

		existingCenter = new Center(
			centerId,
			"Padel Center Barcelona",
			"Calle Principal 123",
			"Barcelona",
			"933123456",
			"barcelona@padelcenter.com",
			null,
			Auditable.newAudit()
		);

		expectedField = new Field(
			UUID.randomUUID(),
			"Court 1",
			"Indoor",
			new BigDecimal("25.00"),
			true,
			existingCenter,
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("Should successfully create a field when center exists")
	void shouldCreateFieldWhenCenterExists() {
		// Given
		when(centerRepository.findById(centerId)).thenReturn(Optional.of(existingCenter));
		when(fieldRepository.save(any(Field.class))).thenReturn(expectedField);

		// When
		Field result = createFieldUseCase.execute(command);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.name()).isEqualTo("Court 1");
		assertThat(result.type()).isEqualTo("Indoor");
		assertThat(result.pricePerHour()).isEqualByComparingTo(new BigDecimal("25.00"));
		assertThat(result.isAvailable()).isTrue();
		assertThat(result.center()).isEqualTo(existingCenter);

		verify(centerRepository, times(1)).findById(centerId);
		verify(fieldRepository, times(1)).save(any(Field.class));
	}

	@Test
	@DisplayName("Should throw CenterNotFoundException when center does not exist")
	void shouldThrowExceptionWhenCenterDoesNotExist() {
		// Given
		when(centerRepository.findById(centerId)).thenReturn(Optional.empty());

		// When & Then
		assertThatThrownBy(() -> createFieldUseCase.execute(command))
			.isInstanceOf(CenterNotFoundException.class);

		verify(centerRepository, times(1)).findById(centerId);
		verify(fieldRepository, never()).save(any(Field.class));
	}

	@Test
	@DisplayName("Should create field with correct attributes from command")
	void shouldCreateFieldWithCorrectAttributes() {
		// Given
		when(centerRepository.findById(centerId)).thenReturn(Optional.of(existingCenter));
		when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Field> fieldCaptor = ArgumentCaptor.forClass(Field.class);

		// When
		createFieldUseCase.execute(command);

		// Then
		verify(fieldRepository).save(fieldCaptor.capture());
		Field capturedField = fieldCaptor.getValue();

		assertThat(capturedField.name()).isEqualTo("Court 1");
		assertThat(capturedField.type()).isEqualTo("Indoor");
		assertThat(capturedField.pricePerHour()).isEqualByComparingTo(new BigDecimal("25.00"));
		assertThat(capturedField.isAvailable()).isTrue();
		assertThat(capturedField.center()).isEqualTo(existingCenter);
		assertThat(capturedField.audit()).isNotNull();
	}

	@Test
	@DisplayName("Should create field with null id before save")
	void shouldCreateFieldWithNullId() {
		// Given
		when(centerRepository.findById(centerId)).thenReturn(Optional.of(existingCenter));
		when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Field> fieldCaptor = ArgumentCaptor.forClass(Field.class);

		// When
		createFieldUseCase.execute(command);

		// Then
		verify(fieldRepository).save(fieldCaptor.capture());
		Field capturedField = fieldCaptor.getValue();

		assertThat(capturedField.id()).isNull();
	}

	@Test
	@DisplayName("Should create field with new audit information")
	void shouldCreateFieldWithNewAudit() {
		// Given
		when(centerRepository.findById(centerId)).thenReturn(Optional.of(existingCenter));
		when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArgumentCaptor<Field> fieldCaptor = ArgumentCaptor.forClass(Field.class);

		// When
		createFieldUseCase.execute(command);

		// Then
		verify(fieldRepository).save(fieldCaptor.capture());
		Field capturedField = fieldCaptor.getValue();

		assertThat(capturedField.audit()).isNotNull();
		assertThat(capturedField.audit().createdAt()).isNotNull();
		assertThat(capturedField.audit().modifiedAt()).isNotNull();
	}

	@Test
	@DisplayName("Should return the saved field")
	void shouldReturnSavedField() {
		// Given
		Field savedField = new Field(
			UUID.randomUUID(),
			"Court 1",
			"Indoor",
			new BigDecimal("25.00"),
			true,
			existingCenter,
			Auditable.newAudit()
		);

		when(centerRepository.findById(centerId)).thenReturn(Optional.of(existingCenter));
		when(fieldRepository.save(any(Field.class))).thenReturn(savedField);

		// When
		Field result = createFieldUseCase.execute(command);

		// Then
		assertThat(result).isEqualTo(savedField);
		assertThat(result.id()).isNotNull();
	}
}
