package com.bookings.padelcenter.infrastructure.inbound.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

@JsonInclude(Include.NON_NULL)
public record AuditableResponse(
	UUID createdBy,
	Instant createdAt,
	UUID modifiedBy,
	Instant modifiedAt,
	UUID deletedBy,
	Instant deletedAt
) {}
