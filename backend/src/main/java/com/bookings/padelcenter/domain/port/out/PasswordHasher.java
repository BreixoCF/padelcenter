package com.bookings.padelcenter.domain.port.out;

/**
 * Driven port: defines the contract for password hashing operations.
 *
 * <p>Implementations must use a one-way, cryptographically secure algorithm
 * (e.g. BCrypt). The domain layer remains agnostic of the specific algorithm.
 */
public interface PasswordHasher {

    /**
     * Hashes a plain-text password.
     *
     * @param plainPassword the raw password; must not be {@code null}
     * @return the encoded password hash; never {@code null}
     */
    String hash(String plainPassword);

    /**
     * Verifies whether a plain-text password matches a stored hash.
     *
     * @param plainPassword  the raw password to check; must not be {@code null}
     * @param hashedPassword the stored password hash; must not be {@code null}
     * @return {@code true} if they match, {@code false} otherwise
     */
    boolean matches(String plainPassword, String hashedPassword);
}
