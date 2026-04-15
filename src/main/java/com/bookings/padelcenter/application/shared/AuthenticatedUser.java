package com.bookings.padelcenter.application.shared;

import java.util.UUID;

/**
 * Carries the identity of the authenticated user performing a write operation.
 *
 * <p>Passed explicitly through commands so that the domain and application layers remain
 * decoupled from {@code SecurityContextHolder}. The web adapter extracts the user from
 * the JWT and injects it here; commands must not call the security context directly.
 *
 * @param userId the authenticated user's identifier, or {@code null} when security is not
 *               yet wired (Phase 8 TODO).
 */
public record AuthenticatedUser(UUID userId) {}
