package com.bookings.padelcenter.application.dto;

import com.bookings.padelcenter.domain.model.Match;

public record MatchHistoryItem(
	Match match,
	String tournamentName,
	String pairAName,
	String pairBName
) {}
