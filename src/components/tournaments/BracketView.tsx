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

interface Pair {
  pairId: string;
  teamName?: string;
}

interface BracketViewProps {
  matches: Match[];
  pairs?: Pair[];
  format: string;
}

export default function BracketView({ matches, pairs = [], format }: BracketViewProps) {
  const nameMap: Record<string, string> = {};
  for (const p of pairs) {
    if (p.pairId && p.teamName) nameMap[p.pairId] = p.teamName;
  }

  const pairLabel = (id?: string) => {
    if (!id) return 'Por determinar';
    return nameMap[id] || id.slice(0, 8);
  };

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
              <span className="font-medium text-slate-700">{pairLabel(m.pairAId)}</span>
              <span className="mx-2 text-slate-300">vs</span>
              <span className="font-medium text-slate-700">{pairLabel(m.pairBId)}</span>
            </div>
            {m.result ? (
              <span className="text-sm font-semibold tabular-nums">
                {m.result.scoreA} — {m.result.scoreB}
              </span>
            ) : (
              <span className="text-xs text-slate-400">{m.status}</span>
            )}
          </div>
        ))}
        {matches.length === 0 && (
          <p className="text-sm text-slate-500 py-4 text-center">Aún no hay partidos generados</p>
        )}
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
                <div key={m.matchId} className="border rounded-lg p-3 w-52 bg-white">
                  <div className={`text-xs p-1.5 rounded mb-1 truncate ${
                    m.result?.winnerPairId === m.pairAId
                      ? 'bg-green-50 font-medium text-green-800'
                      : 'text-slate-600'
                  }`}>
                    {pairLabel(m.pairAId)}
                  </div>
                  <div className={`text-xs p-1.5 rounded truncate ${
                    m.result?.winnerPairId === m.pairBId
                      ? 'bg-green-50 font-medium text-green-800'
                      : 'text-slate-600'
                  }`}>
                    {pairLabel(m.pairBId)}
                  </div>
                  {m.result && (
                    <div className="text-xs text-center text-slate-400 mt-1 tabular-nums">
                      {m.result.scoreA} — {m.result.scoreB}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        ))}
        {roundNumbers.length === 0 && (
          <p className="text-sm text-slate-500 py-4">Aún no hay cuadro generado</p>
        )}
      </div>
    </div>
  );
}
