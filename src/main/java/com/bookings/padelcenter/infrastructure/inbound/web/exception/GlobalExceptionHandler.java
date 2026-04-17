package com.bookings.padelcenter.infrastructure.inbound.web.exception;

import com.bookings.padelcenter.domain.exception.BookingAlreadyCancelledException;
import com.bookings.padelcenter.domain.exception.BookingOverlapException;
import com.bookings.padelcenter.domain.exception.EmailAlreadyExistsException;
import com.bookings.padelcenter.domain.exception.FieldNotAvailableException;
import com.bookings.padelcenter.domain.exception.InvalidMatchResultException;
import com.bookings.padelcenter.domain.exception.InvalidPasswordException;
import com.bookings.padelcenter.domain.exception.InvalidTournamentStatusException;
import com.bookings.padelcenter.domain.exception.PairAlreadyRegisteredException;
import com.bookings.padelcenter.domain.exception.PlayerAlreadyInPairException;
import com.bookings.padelcenter.domain.exception.ResourceNotFoundException;
import com.bookings.padelcenter.domain.exception.UnauthorizedException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Resource Not Found");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
		var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						error -> error.getField(),
						error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
						(existing, replacement) -> existing
				));
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
		problem.setTitle("Bad Request");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		problem.setProperty("errors", fieldErrors);
		return problem;
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ProblemDetail handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Email Already Exists");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(FieldNotAvailableException.class)
	public ProblemDetail handleFieldNotAvailableException(FieldNotAvailableException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
		problem.setTitle("Field Not Available");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(BookingOverlapException.class)
	public ProblemDetail handleBookingOverlapException(BookingOverlapException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Booking Overlap");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(BookingAlreadyCancelledException.class)
	public ProblemDetail handleBookingAlreadyCancelledException(BookingAlreadyCancelledException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Booking Already Cancelled");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
				"You do not have permission to perform this action");
		problem.setTitle("Forbidden");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ProblemDetail handleUnauthorizedException(UnauthorizedException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
		problem.setTitle("Unauthorized");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(InvalidPasswordException.class)
	public ProblemDetail handleInvalidPasswordException(InvalidPasswordException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
		problem.setTitle("Invalid Password");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(InvalidTournamentStatusException.class)
	public ProblemDetail handleInvalidTournamentStatusException(InvalidTournamentStatusException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
		problem.setTitle("Invalid Tournament Status");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(PairAlreadyRegisteredException.class)
	public ProblemDetail handlePairAlreadyRegisteredException(PairAlreadyRegisteredException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Pair Already Registered");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(PlayerAlreadyInPairException.class)
	public ProblemDetail handlePlayerAlreadyInPairException(PlayerAlreadyInPairException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Player Already In Pair");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(InvalidMatchResultException.class)
	public ProblemDetail handleInvalidMatchResultException(InvalidMatchResultException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
		problem.setTitle("Invalid Match Result");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
		String detailedMessage = extractPostgresDetail(ex);
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detailedMessage);
		problem.setTitle("Data Integrity Violation");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleGlobalException(Exception ex) {
		var problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"An internal server error occurred. Please try again later."
		);
		problem.setTitle("Internal Server Error");
		problem.setType(URI.create("about:blank"));
		problem.setProperty("timestamp", Instant.now());
		return problem;
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
