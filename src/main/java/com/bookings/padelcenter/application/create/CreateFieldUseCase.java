package com.bookings.padelcenter.application.create;

import com.bookings.padelcenter.application.command.CreateFieldCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.CenterNotFoundException;
import com.bookings.padelcenter.domain.model.Auditable;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.repository.CenterRepository;
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
public class CreateFieldUseCase implements CommandUseCase<CreateFieldCommand, Field> {

		private final CenterRepository centerRepository;
		private final FieldRepository fieldRepository;

		@PreAuthorize("hasRole('ADMIN')")
		public Field execute(CreateFieldCommand command) {
			log.debug("field.create.start centerId={} name={} type={} pricePerHour={}",
				command.centerId(), command.name(), command.type(), command.pricePerHour());

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
			var field = fieldRepository.save(fieldToCreate);

			log.info("field.created fieldId={} centerId={} name={} pricePerHour={}",
				field.fieldId(), field.center().centerId(), field.name(), field.pricePerHour());

			return field;
		}
}
