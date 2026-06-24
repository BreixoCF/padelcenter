package com.bookings.padelcenter.application.delete;

import com.bookings.padelcenter.application.command.DeleteCenterCommand;
import com.bookings.padelcenter.application.shared.AuthenticatedUser;
import com.bookings.padelcenter.domain.exception.CenterHasActiveFieldsException;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteCenterUseCase Tests")
class DeleteCenterUseCaseTest {

	@Mock
	private CenterRepository centerRepository;

	@Mock
	private FieldRepository fieldRepository;

	@InjectMocks
	private DeleteCenterUseCase deleteCenterUseCase;

	private UUID centerId;
	private UUID deletedBy;
	private DeleteCenterCommand command;
	private Center existingCenter;

	@BeforeEach
	void setUp() {
		centerId = UUID.randomUUID();
		deletedBy = UUID.randomUUID();
		command = new DeleteCenterCommand(centerId, new AuthenticatedUser(deletedBy));
		existingCenter = new Center(
			centerId,
			"Padel Club Madrid",
			"Calle Mayor 1",
			"Madrid",
			"910000001",
			"madrid@padelclub.com",
			null,
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("deleteCenter_existingCenter_softDeletes")
	void deleteCenter_existingCenter_softDeletes() {
		when(centerRepository.findById(centerId)).thenReturn(Optional.of(existingCenter));
		when(fieldRepository.findByCenterId(centerId)).thenReturn(List.of());
		when(centerRepository.save(any(Center.class))).thenAnswer(inv -> inv.getArgument(0));

		var result = deleteCenterUseCase.execute(command);

		assertThat(result.centerId()).isEqualTo(centerId);
		assertThat(result.audit().deletedBy()).isEqualTo(deletedBy);
		assertThat(result.audit().deletedAt()).isNotNull();
		verify(centerRepository).findById(centerId);
		verify(centerRepository).save(any(Center.class));
	}

	@Test
	@DisplayName("deleteCenter_notFound_throwsCenterNotFoundException")
	void deleteCenter_notFound_throwsCenterNotFoundException() {
		when(centerRepository.findById(centerId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> deleteCenterUseCase.execute(command))
			.isInstanceOf(CenterNotFoundException.class)
			.hasMessageContaining(centerId.toString());

		verify(centerRepository).findById(centerId);
		verify(centerRepository, never()).save(any());
	}

	@Test
	@DisplayName("deleteCenter_preservesCenterData")
	void deleteCenter_preservesCenterData() {
		when(centerRepository.findById(centerId)).thenReturn(Optional.of(existingCenter));
		when(fieldRepository.findByCenterId(centerId)).thenReturn(List.of());
		when(centerRepository.save(any(Center.class))).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<Center> captor = ArgumentCaptor.forClass(Center.class);
		deleteCenterUseCase.execute(command);

		verify(centerRepository).save(captor.capture());
		var saved = captor.getValue();
		assertThat(saved.name()).isEqualTo(existingCenter.name());
		assertThat(saved.address()).isEqualTo(existingCenter.address());
		assertThat(saved.audit().deletedBy()).isEqualTo(deletedBy);
	}

	@Test
	@DisplayName("deleteCenter_withActiveFields_throwsCenterHasActiveFieldsException")
	void deleteCenter_withActiveFields_throwsCenterHasActiveFieldsException() {
		var activeField = new Field(UUID.randomUUID(), "Court 1", "Indoor",
			java.math.BigDecimal.valueOf(25.0), true, existingCenter, Auditable.newAudit());

		when(centerRepository.findById(centerId)).thenReturn(Optional.of(existingCenter));
		when(fieldRepository.findByCenterId(centerId)).thenReturn(List.of(activeField));

		assertThatThrownBy(() -> deleteCenterUseCase.execute(command))
			.isInstanceOf(CenterHasActiveFieldsException.class)
			.hasMessageContaining(centerId.toString());

		verify(centerRepository, never()).save(any());
	}
}
