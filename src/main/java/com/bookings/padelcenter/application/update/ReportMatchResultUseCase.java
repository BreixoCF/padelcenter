package com.bookings.padelcenter.application.update;

import com.bookings.padelcenter.application.command.ReportMatchResultCommand;
import com.bookings.padelcenter.application.shared.CommandUseCase;
import com.bookings.padelcenter.domain.exception.InvalidMatchResultException;
import com.bookings.padelcenter.domain.exception.MatchNotFoundException;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.MatchResult;
import com.bookings.padelcenter.domain.model.MatchStatus;
import com.bookings.padelcenter.domain.repository.MatchRepository;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReportMatchResultUseCase implements CommandUseCase<ReportMatchResultCommand, Match> {

	private final MatchRepository matchRepository;
	private final TournamentRepository tournamentRepository;

	@NonNull
	@Override
	public Match execute(ReportMatchResultCommand command) {
		var match = matchRepository.findById(command.matchId())
				.orElseThrow(() -> new MatchNotFoundException(command.matchId()));

		if (match.status() != MatchStatus.SCHEDULED && match.status() != MatchStatus.IN_PROGRESS) {
			throw new InvalidMatchResultException(
					"Cannot report result for match in status: " + match.status());
		}

		var pairA = tournamentRepository.findPairById(match.pairAId())
				.orElseThrow(() -> new InvalidMatchResultException(
						"Match pair A not found: " + match.pairAId()));
		var pairB = tournamentRepository.findPairById(match.pairBId())
				.orElseThrow(() -> new InvalidMatchResultException(
						"Match pair B not found: " + match.pairBId()));

		if (!match.isReportableBy(command.reportedBy(), pairA, pairB)) {
			throw new InvalidMatchResultException(
					"User " + command.reportedBy() + " is not a player in this match");
		}

		var result = new MatchResult(
				command.winnerPairId(),
				command.scoreA(),
				command.scoreB(),
				command.reportedBy(),
				Instant.now()
		);

		var completed = match.withResult(result);
		var saved = matchRepository.save(completed);
		log.info("match.result.reported matchId={} winner={}", saved.matchId(), command.winnerPairId());
		return saved;
	}
}
