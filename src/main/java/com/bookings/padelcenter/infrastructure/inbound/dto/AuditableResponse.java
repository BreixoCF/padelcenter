package com.bookings.padelcenter.infrastructure.inbound.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

@JsonInclude(Include.NON_NULL)
public record AuditableResponse(
	UUID createdBy,
	Instant createdAt,
	UUID lastModifiedBy,
	Instant lastModifiedAt,
	UUID deletedBy,
	Instant deletedAt
) {}
