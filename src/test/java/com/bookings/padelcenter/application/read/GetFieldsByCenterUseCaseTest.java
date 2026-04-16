package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetFieldsByCenterQuery;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.*;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFieldsByCenterUseCase")
class GetFieldsByCenterUseCaseTest {

	@Mock CenterRepository centerRepository;
	@Mock FieldRepository fieldRepository;
	@InjectMocks GetFieldsByCenterUseCase useCase;

	private static final UUID CENTER_ID = UUID.randomUUID();
	private final Center center = new Center(CENTER_ID, "Padel BCN", "Calle 1", "Barcelona",
			"933000001", "bcn@padel.com", null, Auditable.newAudit());

	@Test
	@DisplayName("should return paged fields for existing center")
	void getFields_existingCenter_returnsPagedFields() {
		// given
		var fields = List.of(
				field("Court 1", "Indoor", true),
				field("Court 2", "Outdoor", true));
		var page = new PageResult<>(fields, 0, 20, 2L, 1);

		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
		given(fieldRepository.findByCenterWithFilters(CENTER_ID, null, null, 0, 20))
				.willReturn(page);

		// when
		var result = useCase.execute(new GetFieldsByCenterQuery(CENTER_ID, null, null, 0, 20));

		// then
		assertThat(result.content()).hasSize(2);
		assertThat(result.totalElements()).isEqualTo(2L);
	}

	@Test
	@DisplayName("should filter by type when type parameter is provided")
	void getFields_withTypeFilter_returnsFilteredFields() {
		// given
		var fields = List.of(field("Court 1", "Indoor", true));
		var page = new PageResult<>(fields, 0, 20, 1L, 1);

		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
		given(fieldRepository.findByCenterWithFilters(CENTER_ID, "Indoor", null, 0, 20))
				.willReturn(page);

		// when
		var result = useCase.execute(new GetFieldsByCenterQuery(CENTER_ID, "Indoor", null, 0, 20));

		// then
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().getFirst().type()).isEqualTo("Indoor");
	}

	@Test
	@DisplayName("should filter by availability when available parameter is provided")
	void getFields_withAvailableFilter_returnsOnlyAvailableFields() {
		// given
		var fields = List.of(field("Court 1", "Indoor", true));
		var page = new PageResult<>(fields, 0, 20, 1L, 1);

		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));
		given(fieldRepository.findByCenterWithFilters(CENTER_ID, null, true, 0, 20))
				.willReturn(page);

		// when
		var result = useCase.execute(new GetFieldsByCenterQuery(CENTER_ID, null, true, 0, 20));

		// then
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().getFirst().isAvailable()).isTrue();
	}

	@Test
	@DisplayName("should throw CenterNotFoundException when center does not exist")
	void getFields_centerNotFound_throwsCenterNotFoundException() {
		// given
		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> useCase.execute(
				new GetFieldsByCenterQuery(CENTER_ID, null, null, 0, 20)))
				.isInstanceOf(CenterNotFoundException.class);
	}

	private Field field(String name, String type, boolean available) {
		return new Field(UUID.randomUUID(), name, type, BigDecimal.valueOf(25), available,
				center, Auditable.newAudit());
	}
}
