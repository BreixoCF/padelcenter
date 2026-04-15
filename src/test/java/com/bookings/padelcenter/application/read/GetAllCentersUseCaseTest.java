package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetAllCentersQuery;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAllCentersUseCaseTest {

	@Mock
	private CenterRepository centerRepository;

	@InjectMocks
	private GetAllCentersUseCase getAllCentersUseCase;

	@Test
	@DisplayName("execute_validQuery_returnsPageResultFromRepository")
	void execute_validQuery_returnsPageResultFromRepository() {
		var center = new Center(UUID.randomUUID(), "Center A", "Calle 1", "Madrid",
				"911000000", "a@test.com", null, Auditable.newAudit());
		var expected = new PageResult<>(List.of(center), 0, 10, 1L, 1);
		var query = new GetAllCentersQuery(0, 10);

		when(centerRepository.findAll(0, 10)).thenReturn(expected);

		var result = getAllCentersUseCase.execute(query);

		assertThat(result).isEqualTo(expected);
		assertThat(result.content()).hasSize(1);
		assertThat(result.totalElements()).isEqualTo(1L);
		verify(centerRepository).findAll(0, 10);
	}

	@Test
	@DisplayName("execute_emptyRepository_returnsEmptyPage")
	void execute_emptyRepository_returnsEmptyPage() {
		var query = new GetAllCentersQuery(0, 10);
		var empty = new PageResult<Center>(List.of(), 0, 10, 0L, 0);

		when(centerRepository.findAll(0, 10)).thenReturn(empty);

		var result = getAllCentersUseCase.execute(query);

		assertThat(result.content()).isEmpty();
		assertThat(result.totalElements()).isZero();
	}

	@Test
	@DisplayName("execute_secondPage_delegatesPaginationToRepository")
	void execute_secondPage_delegatesPaginationToRepository() {
		var query = new GetAllCentersQuery(1, 5);
		var expected = new PageResult<Center>(List.of(), 1, 5, 0L, 0);

		when(centerRepository.findAll(1, 5)).thenReturn(expected);

		var result = getAllCentersUseCase.execute(query);

		assertThat(result.page()).isEqualTo(1);
		assertThat(result.size()).isEqualTo(5);
		verify(centerRepository).findAll(1, 5);
	}
}
