package com.bookings.padelcenter.infrastructure.inbound.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record CreateCenterResponse(
	UUID id,
	String name,
	String address,
	String city,
	String phoneNumber,
	String email,
	AuditableResponse audit
) {}
