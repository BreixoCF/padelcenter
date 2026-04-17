package com.bookings.padelcenter.application.command;

import java.util.UUID;

public record ConfirmPairCommand(UUID tournamentId, UUID pairId) {}
