package com.bookings.padelcenter.infrastructure.inbound.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonInclude(NON_NULL)
public record CreateUserResponse(
	UUID id,
	String firstName,
	String lastName,
	String email,
	String phoneNumber,
	AuditableResponse audit
) {}
