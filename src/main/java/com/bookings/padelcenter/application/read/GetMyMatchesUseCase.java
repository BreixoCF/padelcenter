package com.bookings.padelcenter.application.read;

import com.bookings.padelcenter.application.dto.MatchHistoryItem;
import com.bookings.padelcenter.application.query.GetMyMatchesQuery;
import com.bookings.padelcenter.application.shared.QueryUseCase;
import com.bookings.padelcenter.domain.model.Match;
import com.bookings.padelcenter.domain.model.PageResult;
import com.bookings.padelcenter.domain.model.TournamentPair;
import com.bookings.padelcenter.domain.repository.MatchRepository;
import com.bookings.padelcenter.domain.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyMatchesUseCase implements QueryUseCase<GetMyMatchesQuery, PageResult<MatchHistoryItem>> {

	private final TournamentRepository tournamentRepository;
	private final MatchRepository matchRepository;

	@NonNull
	@Override
	public PageResult<MatchHistoryItem> execute(GetMyMatchesQuery query) {
		List<TournamentPair> myPairs = tournamentRepository.findPairsByUserId(query.userId());

		if (myPairs.isEmpty()) {
			return new PageResult<>(List.of(), query.page(), query.size(), 0, 0);
		}

		List<UUID> pairIds = myPairs.stream().map(TournamentPair::pairId).toList();
		Map<UUID, String> pairTeamNames = myPairs.stream()
				.collect(Collectors.toMap(TournamentPair::pairId, TournamentPair::teamName));

		List<Match> allMatches = matchRepository.findByPairIdIn(pairIds);

		long totalElements = allMatches.size();
		int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / query.size());
		int from = query.page() * query.size();
		int to = (int) Math.min(from + query.size(), totalElements);
		List<Match> pageMatches = from >= totalElements ? List.of() : allMatches.subList(from, to);

		List<UUID> tournamentIds = pageMatches.stream().map(Match::tournamentId).distinct().toList();
		Map<UUID, String> tournamentNames = tournamentIds.stream()
				.flatMap(tid -> tournamentRepository.findById(tid).stream())
				.collect(Collectors.toMap(t -> t.tournamentId(), t -> t.name()));

		List<MatchHistoryItem> items = pageMatches.stream().map(match -> new MatchHistoryItem(
				match,
				tournamentNames.getOrDefault(match.tournamentId(), ""),
				pairTeamNames.getOrDefault(match.pairAId(), ""),
				pairTeamNames.getOrDefault(match.pairBId(), "")
		)).toList();

		return new PageResult<>(items, query.page(), query.size(), totalElements, totalPages);
	}
}
