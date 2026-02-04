package com.bookings.padelcenter.infrastructure.inbound.web.exception;

import com.bookings.padelcenter.domain.exception.BookingAlreadyCancelledException;
import com.bookings.padelcenter.domain.exception.InvalidPasswordException;
import com.bookings.padelcenter.domain.exception.ResourceNotFoundException;
import org.jspecify.annotations.NonNull;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

	public record ApiErrorDetails(
			Instant timestamp,
			String message,
			String details
	) {
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<@NonNull ApiErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
		ApiErrorDetails errorDetails = new ApiErrorDetails(
				Instant.now(),
				ex.getMessage(),
				request.getDescription(false)
		);
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<@NonNull Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(BookingAlreadyCancelledException.class)
	public ResponseEntity<@NonNull ApiErrorDetails> handleBookingAlreadyCancelledException(BookingAlreadyCancelledException ex, WebRequest request) {
		ApiErrorDetails errorDetails = new ApiErrorDetails(
				Instant.now(),
				ex.getMessage(),
				request.getDescription(false)
		);
		return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<@NonNull ApiErrorDetails> handleInvalidPasswordException(InvalidPasswordException ex, WebRequest request) {
		ApiErrorDetails errorDetails = new ApiErrorDetails(
				Instant.now(),
				ex.getMessage(),
				request.getDescription(false)
		);
		return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<@NonNull ApiErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
		ApiErrorDetails errorDetails = new ApiErrorDetails(
				Instant.now(),
				"An internal server error occurred. Please try again later.",
				request.getDescription(false) + ". Cause: " + ex.getMessage()
		);
		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<@NonNull ApiErrorDetails> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
		String detailedMessage = extractPostgresDetail(ex);
		ApiErrorDetails errorDetails = new ApiErrorDetails(
				Instant.now(),
				"Data Integrity Violation: " + detailedMessage,
				request.getDescription(false)
		);
		return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
	}

	private String extractPostgresDetail(DataIntegrityViolationException ex) {
		return Optional.ofNullable(ex.getRootCause())
				.filter(rootCause -> rootCause instanceof PSQLException)
				.map(rootCause -> (PSQLException) rootCause)
				.map(PSQLException::getServerErrorMessage)
				.map(serverError -> {
					String detail = serverError.getDetail();
					String constraintName = serverError.getConstraint();
					if (constraintName != null && detail != null) {
						return String.format("Constraint '%s' violated. Detail: %s", constraintName, detail);
					}
					return "Database constraint violation occurred.";
				})
				.orElseGet(() -> {
					String causeMessage = Optional.ofNullable(ex.getRootCause())
							.map(Throwable::getMessage)
							.orElse("Unknown database error.");
					return "Database restriction violation. Caused by: " + causeMessage;
				});
	}
}