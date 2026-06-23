package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetFieldByIdQuery;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFieldByIdUseCaseTest {

	@Mock
	private FieldRepository fieldRepository;

	@InjectMocks
	private GetFieldByIdUseCase getFieldByIdUseCase;

	@Test
	@DisplayName("execute_existingId_returnsField")
	void execute_existingId_returnsField() {
		var fieldId = UUID.randomUUID();
		var center = new Center(UUID.randomUUID(), "Center", "Addr", "City",
				null, null, null, Auditable.newAudit());
		var field = new Field(fieldId, "Court 1", "Outdoor",
				new BigDecimal("15.00"), true, center, Auditable.newAudit());
		var query = new GetFieldByIdQuery(fieldId);

		when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(field));

		var result = getFieldByIdUseCase.execute(query);

		assertThat(result).isEqualTo(field);
		assertThat(result.fieldId()).isEqualTo(fieldId);
		verify(fieldRepository).findById(fieldId);
	}

	@Test
	@DisplayName("execute_nonExistingId_throwsFieldNotFoundException")
	void execute_nonExistingId_throwsFieldNotFoundException() {
		var fieldId = UUID.randomUUID();
		var query = new GetFieldByIdQuery(fieldId);

		when(fieldRepository.findById(fieldId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> getFieldByIdUseCase.execute(query))
				.isInstanceOf(FieldNotFoundException.class)
				.hasMessageContaining(fieldId.toString());

		verify(fieldRepository).findById(fieldId);
	}
}
