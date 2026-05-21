'use client';
import { useState } from 'react';
import { useGetMyMatches } from '@/lib/api/generated/auth/auth';
import PageHeader from '@/components/shared/PageHeader';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { GridSkeleton } from '@/components/shared/LoadingState';
import EmptyState from '@/components/shared/EmptyState';
import { Swords } from 'lucide-react';
import type { MatchHistoryItem } from '@/lib/api/generated/models';

const STATUS_LABEL: Record<string, string> = {
  SCHEDULED: 'Programado',
  IN_PROGRESS: 'En juego',
  COMPLETED: 'Completado',
  CANCELLED: 'Cancelado',
};

const STATUS_VARIANT: Record<string, 'default' | 'outline' | 'secondary'> = {
  COMPLETED: 'default',
  IN_PROGRESS: 'secondary',
  SCHEDULED: 'outline',
  CANCELLED: 'outline',
};

export default function MatchesPage() {
  const [page, setPage] = useState(0);

  const { data, isLoading } = useGetMyMatches({ page, size: 15 });

  const matches: MatchHistoryItem[] = data?.content ?? [];

  return (
    <main className="container mx-auto px-4 py-8 max-w-3xl">
      <PageHeader
        title="Mis partidos"
        description={data ? `${data.totalElements} partidos en total` : undefined}
      />

      {isLoading ? (
        <GridSkeleton count={5} />
      ) : matches.length === 0 ? (
        <EmptyState
          icon={Swords}
          title="Sin partidos"
          description="Aún no has jugado ningún partido en un torneo"
        />
      ) : (
        <>
          <div className="space-y-3">
            {matches.map((match) => {
              const isCompleted = match.status === 'COMPLETED';
              const hasResult = !!match.result;
              return (
                <Card key={match.matchId}>
                  <CardContent className="p-4">
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex-1 min-w-0">
                        <p className="text-xs text-slate-500 mb-1 truncate">{match.tournamentName}</p>
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-medium truncate">
                            {match.pairAName || match.pairAId?.slice(0, 8) || '—'}
                          </span>
                          <span className="text-slate-300 shrink-0">vs</span>
                          <span className="text-sm font-medium truncate">
                            {match.pairBName || match.pairBId?.slice(0, 8) || '—'}
                          </span>
                        </div>
                        {match.round && (
                          <p className="text-xs text-slate-400 mt-0.5">
                            Ronda {match.round}
                            {match.groupName && ` · Grupo ${match.groupName}`}
                          </p>
                        )}
                      </div>

                      <div className="flex flex-col items-end gap-1 shrink-0">
                        <Badge variant={STATUS_VARIANT[match.status] ?? 'outline'}>
                          {STATUS_LABEL[match.status] ?? match.status}
                        </Badge>
                        {hasResult && (
                          <span className="text-base font-bold tabular-nums">
                            {match.result!.scoreA} — {match.result!.scoreB}
                          </span>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>

          {data && data.totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-4">
              <Button variant="outline" size="sm"
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}>
                Anterior
              </Button>
              <span className="text-sm text-slate-500 flex items-center px-2">
                {page + 1} / {data.totalPages}
              </span>
              <Button variant="outline" size="sm"
                disabled={page >= data.totalPages - 1}
                onClick={() => setPage(p => p + 1)}>
                Siguiente
              </Button>
            </div>
          )}
        </>
      )}
    </main>
  );
}
