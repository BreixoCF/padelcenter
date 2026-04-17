package com.bookings.padelcenter.application.command;

import java.util.UUID;

public record UpdateFieldAvailabilityCommand(UUID fieldId, boolean available) {}
