package com.bookings.padelcenter.domain.model;

import java.util.UUID;

public record RoundRobinStanding(
	UUID pairId,
	String teamName,
	int played,
	int wins,
	int losses,
	int setsFor,
	int setsAgainst,
	int setDifference
) {}
