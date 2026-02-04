package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateCenterCommand;
import com.bookings.padelcenter.application.mapper.CenterCommandMapper;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCenterUseCase Tests")
class CreateCenterUseCaseTest {

	@Mock
	private CenterRepository centerRepository;

	@Mock
	private CenterCommandMapper centerCommandMapper;

	@InjectMocks
	private CreateCenterUseCase createCenterUseCase;

	private CreateCenterCommand command;
	private Center expectedCenter;

	@BeforeEach
	void setUp() {
		command = new CreateCenterCommand(
			"Padel Center Barcelona",
			"Calle Principal 123",
			"Barcelona",
			"933123456",
			"barcelona@padelcenter.com"
		);

		expectedCenter = new Center(
			UUID.randomUUID(),
			"Padel Center Barcelona",
			"Calle Principal 123",
			"Barcelona",
			"933123456",
			"barcelona@padelcenter.com",
			null,
			Auditable.newAudit()
		);
	}

	@Test
	@DisplayName("Should successfully create a center")
	void shouldCreateCenter() {
		// Given
		when(centerCommandMapper.toDomain(command)).thenReturn(expectedCenter);
		when(centerRepository.save(expectedCenter)).thenReturn(expectedCenter);

		// When
		Center result = createCenterUseCase.execute(command);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.name()).isEqualTo("Padel Center Barcelona");
		assertThat(result.address()).isEqualTo("Calle Principal 123");
		assertThat(result.city()).isEqualTo("Barcelona");
		assertThat(result.phoneNumber()).isEqualTo("933123456");
		assertThat(result.email()).isEqualTo("barcelona@padelcenter.com");

		verify(centerCommandMapper, times(1)).toDomain(command);
		verify(centerRepository, times(1)).save(expectedCenter);
	}

	@Test
	@DisplayName("Should map command to domain using mapper")
	void shouldUseMapperToConvertCommand() {
		// Given
		when(centerCommandMapper.toDomain(command)).thenReturn(expectedCenter);
		when(centerRepository.save(any(Center.class))).thenReturn(expectedCenter);

		// When
		createCenterUseCase.execute(command);

		// Then
		verify(centerCommandMapper).toDomain(command);
	}

	@Test
	@DisplayName("Should save center using repository")
	void shouldSaveCenterUsingRepository() {
		// Given
		when(centerCommandMapper.toDomain(command)).thenReturn(expectedCenter);
		when(centerRepository.save(expectedCenter)).thenReturn(expectedCenter);

		// When
		createCenterUseCase.execute(command);

		// Then
		verify(centerRepository).save(expectedCenter);
	}

	@Test
	@DisplayName("Should return the saved center")
	void shouldReturnSavedCenter() {
		// Given
		Center savedCenter = new Center(
			UUID.randomUUID(),
			"Padel Center Barcelona",
			"Calle Principal 123",
			"Barcelona",
			"933123456",
			"barcelona@padelcenter.com",
			null,
			Auditable.newAudit()
		);

		when(centerCommandMapper.toDomain(command)).thenReturn(expectedCenter);
		when(centerRepository.save(expectedCenter)).thenReturn(savedCenter);

		// When
		Center result = createCenterUseCase.execute(command);

		// Then
		assertThat(result).isEqualTo(savedCenter);
		assertThat(result.id()).isNotNull();
	}
}
