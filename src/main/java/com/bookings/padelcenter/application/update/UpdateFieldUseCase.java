package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.UpdateFieldCommand;
import com.bookings.padelcenter.domain.exception.FieldNotFoundException;
import com.bookings.padelcenter.domain.model.Field;
import com.bookings.padelcenter.domain.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateFieldUseCase {

    private final FieldRepository fieldRepository;

    @Transactional
    public Field execute(UpdateFieldCommand command) {
        var existing = fieldRepository.findById(command.fieldId())
                .orElseThrow(() -> new FieldNotFoundException(command.fieldId()));

        var updated = new Field(
                existing.fieldId(),
                command.name(),
                command.type(),
                command.pricePerHour(),
                command.isAvailable(),
                existing.center(),
                existing.audit()
        );

        var saved = fieldRepository.save(updated);
        log.info("field.updated fieldId={} name={}", command.fieldId(), command.name());
        return saved;
    }
}
