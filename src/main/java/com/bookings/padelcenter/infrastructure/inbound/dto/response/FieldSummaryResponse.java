package com.bookings.padelcenter.infrastructure.inbound.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldSummaryResponse(
	UUID id,
	String name,
	String type,
	CenterSummaryResponse center
) {}
