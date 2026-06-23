package com.bookings.padelcenter.application.delete;

import com.bookings.padelcenter.application.command.DeleteFieldCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteFieldUseCase implements CommandUseCase<DeleteFieldCommand, Field> {

	private final FieldRepository fieldRepository;

	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public Field execute(DeleteFieldCommand command) {
		log.debug("field.delete.start fieldId={} deletedBy={}",
			command.fieldId(), command.authenticatedUser().userId());

		var field = fieldRepository.findById(command.fieldId())
				.orElseThrow(() -> new FieldNotFoundException(command.fieldId()));
		var deletedField = field.delete(command.authenticatedUser().userId());
		var savedField = fieldRepository.save(deletedField);

		log.info("field.deleted fieldId={}", savedField.fieldId());

		return savedField;
	}
}
