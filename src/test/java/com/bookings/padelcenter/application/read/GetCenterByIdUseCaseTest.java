package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.query.GetCenterByIdQuery;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCenterByIdUseCase")
class GetCenterByIdUseCaseTest {

	@Mock CenterRepository centerRepository;
	@InjectMocks GetCenterByIdUseCase useCase;

	private static final UUID CENTER_ID = UUID.randomUUID();
	private final Center center = new Center(CENTER_ID, "Padel BCN", "Calle Mayor 1", "Barcelona",
			"933000001", "bcn@padel.com", null, Auditable.newAudit());

	@Test
	@DisplayName("should return center when it exists")
	void getCenterById_existingCenter_returnsCenter() {
		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.of(center));

		var result = useCase.execute(new GetCenterByIdQuery(CENTER_ID));

		assertThat(result.centerId()).isEqualTo(CENTER_ID);
		assertThat(result.name()).isEqualTo("Padel BCN");
		assertThat(result.city()).isEqualTo("Barcelona");
	}

	@Test
	@DisplayName("should throw CenterNotFoundException when center does not exist")
	void getCenterById_notFound_throwsCenterNotFoundException() {
		given(centerRepository.findById(CENTER_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new GetCenterByIdQuery(CENTER_ID)))
				.isInstanceOf(CenterNotFoundException.class);
	}
}
