package com.bookings.padelcenter.application.service;

import org.springframework.stereotype.Service;

/**
 * Service for password operations.
 * In a real application, this would use a proper password hashing algorithm like BCrypt.
 */
@Service
public class PasswordService {

	/**
	 * Hashes a plain text password.
	 * TODO: In production, use BCrypt or similar: BCryptPasswordEncoder.encode(password)
	 */
	public String hashPassword(String plainPassword) {
		// For now, we're using a simple prefix to simulate hashing
		// In production, replace with: new BCryptPasswordEncoder().encode(plainPassword)
		return "hashed_" + plainPassword;
	}

	/**
	 * Verifies if a plain text password matches a hashed password.
	 * TODO: In production, use BCrypt: BCryptPasswordEncoder.matches(plainPassword, hashedPassword)
	 */
	public boolean verifyPassword(String plainPassword, String hashedPassword) {
		// For now, we're using a simple comparison
		// In production, replace with: new BCryptPasswordEncoder().matches(plainPassword, hashedPassword)
		return hashedPassword.equals("hashed_" + plainPassword);
	}
}
