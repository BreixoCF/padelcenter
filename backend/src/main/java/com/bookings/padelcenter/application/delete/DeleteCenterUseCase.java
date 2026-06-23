package com.bookings.padelcenter.application.delete;

import com.bookings.padelcenter.application.command.DeleteCenterCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.Center;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteCenterUseCase implements CommandUseCase<DeleteCenterCommand, Center> {

	private final CenterRepository centerRepository;

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public Center execute(DeleteCenterCommand command) {
		log.debug("center.delete.start centerId={} deletedBy={}",
			command.centerId(), command.authenticatedUser().userId());

		var center = centerRepository.findById(command.centerId())
				.orElseThrow(() -> new CenterNotFoundException(command.centerId()));
		var deletedCenter = center.delete(command.authenticatedUser().userId());
		var savedCenter = centerRepository.save(deletedCenter);

		log.info("center.deleted centerId={}", savedCenter.centerId());

		return savedCenter;
	}
}
