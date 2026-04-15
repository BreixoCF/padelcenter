package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetAllFieldsQuery;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllFieldsUseCaseTest {

	@Mock
	private FieldRepository fieldRepository;

	@InjectMocks
	private GetAllFieldsUseCase getAllFieldsUseCase;

	private Field buildField() {
		var center = new Center(UUID.randomUUID(), "Center", "Addr", "City",
				null, null, null, Auditable.newAudit());
		return new Field(UUID.randomUUID(), "Court 1", "Indoor",
				new BigDecimal("20.00"), true, center, Auditable.newAudit());
	}

	@Test
	@DisplayName("execute_validQuery_returnsPageResultFromRepository")
	void execute_validQuery_returnsPageResultFromRepository() {
		var field = buildField();
		var expected = new PageResult<>(List.of(field), 0, 10, 1L, 1);
		var query = new GetAllFieldsQuery(0, 10);

		when(fieldRepository.findAll(0, 10)).thenReturn(expected);

		var result = getAllFieldsUseCase.execute(query);

		assertThat(result).isEqualTo(expected);
		assertThat(result.content()).hasSize(1);
		verify(fieldRepository).findAll(0, 10);
	}

	@Test
	@DisplayName("execute_emptyRepository_returnsEmptyPage")
	void execute_emptyRepository_returnsEmptyPage() {
		var query = new GetAllFieldsQuery(0, 20);
		var empty = new PageResult<Field>(List.of(), 0, 20, 0L, 0);

		when(fieldRepository.findAll(0, 20)).thenReturn(empty);

		var result = getAllFieldsUseCase.execute(query);

		assertThat(result.content()).isEmpty();
		assertThat(result.totalElements()).isZero();
	}

	@Test
	@DisplayName("execute_secondPage_delegatesPaginationToRepository")
	void execute_secondPage_delegatesPaginationToRepository() {
		var query = new GetAllFieldsQuery(2, 5);
		var expected = new PageResult<Field>(List.of(), 2, 5, 0L, 0);

		when(fieldRepository.findAll(2, 5)).thenReturn(expected);

		var result = getAllFieldsUseCase.execute(query);

		assertThat(result.page()).isEqualTo(2);
		assertThat(result.size()).isEqualTo(5);
		verify(fieldRepository).findAll(2, 5);
	}
}
