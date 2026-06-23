package com.bookings.padelcenter.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateFieldCommand(
        UUID fieldId,
        String name,
        String type,
        BigDecimal pricePerHour,
        boolean isAvailable
) {}
