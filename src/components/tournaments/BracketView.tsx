'use client';

interface MatchResult {
  winnerPairId: string;
  scoreA: number;
  scoreB: number;
}

interface Match {
  matchId: string;
  round: number;
  groupName?: string;
  pairAId?: string;
  pairBId?: string;
  status: string;
  result?: MatchResult;
}

interface BracketViewProps {
  matches: Match[];
  format: string;
}

export default function BracketView({ matches, format }: BracketViewProps) {
  const rounds = matches.reduce((acc, m) => {
    if (!acc[m.round]) acc[m.round] = [];
    acc[m.round].push(m);
    return acc;
  }, {} as Record<number, Match[]>);

  const roundNumbers = Object.keys(rounds).map(Number).sort((a, b) => a - b);

  if (format === 'ROUND_ROBIN') {
    return (
      <div className="space-y-2">
        {matches.map(m => (
          <div key={m.matchId}
            className="border rounded-lg p-3 flex items-center justify-between">
            <div className="text-sm">
              <span className="font-mono text-slate-400 text-xs">
                {m.pairAId?.slice(0, 8) ?? 'TBD'}
              </span>
              <span className="mx-2 text-slate-300">vs</span>
              <span className="font-mono text-slate-400 text-xs">
                {m.pairBId?.slice(0, 8) ?? 'TBD'}
              </span>
            </div>
            {m.result ? (
              <span className="text-sm font-medium">
                {m.result.scoreA} — {m.result.scoreB}
              </span>
            ) : (
              <span className="text-xs text-slate-400">{m.status}</span>
            )}
          </div>
        ))}
      </div>
    );
  }

  const maxRound = roundNumbers.length > 0 ? Math.max(...roundNumbers) : 0;

  return (
    <div className="overflow-x-auto">
      <div className="flex gap-6 min-w-max pb-4">
        {roundNumbers.map(round => (
          <div key={round} className="flex flex-col gap-3">
            <p className="text-xs font-medium text-slate-500 text-center uppercase tracking-wide">
              {round === maxRound
                ? 'Final'
                : round === maxRound - 1
                  ? 'Semifinal'
                  : `Ronda ${round}`}
            </p>
            <div className="flex flex-col justify-around gap-4 flex-1">
              {rounds[round].map(m => (
                <div key={m.matchId} className="border rounded-lg p-3 w-48 bg-white">
                  <div className={`text-xs p-1.5 rounded mb-1 ${
                    m.result?.winnerPairId === m.pairAId
                      ? 'bg-green-50 font-medium text-green-800'
                      : 'text-slate-600'
                  }`}>
                    {m.pairAId ? m.pairAId.slice(0, 8) : 'Por determinar'}
                  </div>
                  <div className={`text-xs p-1.5 rounded ${
                    m.result?.winnerPairId === m.pairBId
                      ? 'bg-green-50 font-medium text-green-800'
                      : 'text-slate-600'
                  }`}>
                    {m.pairBId ? m.pairBId.slice(0, 8) : 'Por determinar'}
                  </div>
                  {m.result && (
                    <div className="text-xs text-center text-slate-400 mt-1">
                      {m.result.scoreA} — {m.result.scoreB}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
