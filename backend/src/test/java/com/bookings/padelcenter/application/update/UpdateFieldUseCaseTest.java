package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateFieldCommand;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
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
@DisplayName("UpdateFieldUseCase Tests")
class UpdateFieldUseCaseTest {

    @Mock
    private FieldRepository fieldRepository;

    @InjectMocks
    private UpdateFieldUseCase updateFieldUseCase;

    private UUID fieldId;
    private Field existingField;
    private UpdateFieldCommand command;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        var center = new Center(UUID.randomUUID(), "Test Center", "Addr", "City",
                null, null, null, Auditable.newAudit());
        existingField = new Field(fieldId, "Old Name", "Outdoor",
                new BigDecimal("20.00"), true, center, Auditable.newAudit());
        command = new UpdateFieldCommand(fieldId, "Court 1", "Indoor",
                new BigDecimal("30.00"), false);
    }

    @Test
    @DisplayName("execute_existingField_updatesAllFields")
    void execute_existingField_updatesAllFields() {
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(existingField));
        when(fieldRepository.save(any(Field.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = updateFieldUseCase.execute(command);

        assertThat(result.name()).isEqualTo("Court 1");
        assertThat(result.type()).isEqualTo("Indoor");
        assertThat(result.pricePerHour()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(result.isAvailable()).isFalse();
        verify(fieldRepository).save(any(Field.class));
    }

    @Test
    @DisplayName("execute_existingField_preservesCenterAndId")
    void execute_existingField_preservesCenterAndId() {
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(existingField));
        when(fieldRepository.save(any(Field.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Field> captor = ArgumentCaptor.forClass(Field.class);

        updateFieldUseCase.execute(command);

        verify(fieldRepository).save(captor.capture());
        Field saved = captor.getValue();
        assertThat(saved.fieldId()).isEqualTo(fieldId);
        assertThat(saved.center()).isEqualTo(existingField.center());
        assertThat(saved.audit()).isEqualTo(existingField.audit());
    }

    @Test
    @DisplayName("execute_nonExistingField_throwsFieldNotFoundException")
    void execute_nonExistingField_throwsFieldNotFoundException() {
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateFieldUseCase.execute(command))
                .isInstanceOf(FieldNotFoundException.class);

        verify(fieldRepository, never()).save(any());
    }

    @Test
    @DisplayName("execute_validCommand_returnsSavedField")
    void execute_validCommand_returnsSavedField() {
        var savedField = new Field(fieldId, "Court 1", "Indoor",
                new BigDecimal("30.00"), false, existingField.center(), existingField.audit());
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(existingField));
        when(fieldRepository.save(any(Field.class))).thenReturn(savedField);

        var result = updateFieldUseCase.execute(command);

        assertThat(result).isEqualTo(savedField);
    }
}
