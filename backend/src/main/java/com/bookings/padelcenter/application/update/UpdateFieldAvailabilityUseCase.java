package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateFieldAvailabilityCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateFieldAvailabilityUseCase implements CommandUseCase<UpdateFieldAvailabilityCommand, Field> {

	private final FieldRepository fieldRepository;

	@NonNull
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public Field execute(UpdateFieldAvailabilityCommand command) {
		var existing = fieldRepository.findById(command.fieldId())
				.orElseThrow(() -> new FieldNotFoundException(command.fieldId()));

		var updated = new Field(
				existing.fieldId(),
				existing.name(),
				existing.type(),
				existing.pricePerHour(),
				command.available(),
				existing.center(),
				existing.audit()
		);

		var saved = fieldRepository.save(updated);
		log.info("field.availability.updated fieldId={} available={}", command.fieldId(), command.available());
		return saved;
	}
}
