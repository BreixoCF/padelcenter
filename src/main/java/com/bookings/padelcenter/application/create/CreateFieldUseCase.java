package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateFieldCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.repository.CenterRepository;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateFieldUseCase implements CommandUseCase<CreateFieldCommand, Field> {

		private final CenterRepository centerRepository;
		private final FieldRepository fieldRepository;

		@PreAuthorize("hasRole('ADMIN')")
		public Field execute(CreateFieldCommand command) {
			var center = centerRepository.findById(command.centerId())
					.orElseThrow(() -> new CenterNotFoundException(command.centerId()));
			var fieldToCreate = new Field(
					null,
					command.name(),
					command.type(),
					command.pricePerHour(),
					command.isAvailable(),
					center,
					Auditable.newAudit()
			);
			return fieldRepository.save(fieldToCreate);
		}
}
