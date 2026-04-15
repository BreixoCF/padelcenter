package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateCenterCommand;
import com.bookings.padelcenter.application.mapper.CenterCommandMapper;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateCenterUseCase implements CommandUseCase<CreateCenterCommand, Center> {

	private final CenterRepository centerRepository;
	private final CenterCommandMapper centerCommandMapper;

	@PreAuthorize("hasRole('ADMIN')")
	public Center execute(CreateCenterCommand command) {
		var centerToCreate = centerCommandMapper.toDomain(command);
		return centerRepository.save(centerToCreate);
	}
}
