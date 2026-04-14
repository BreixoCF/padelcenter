package com.bookings.padelcenter.infrastructure.inbound.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record UpdateUserResponse(
	UUID id,
	String firstName,
	String lastName,
	String email,
	String phoneNumber,
	AuditableResponse audit
) {}
