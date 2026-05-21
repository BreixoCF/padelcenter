'use client';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Calendar, Users } from 'lucide-react';
import Link from 'next/link';
import StatusBadge from '@/components/shared/StatusBadge';
import type { TournamentResponse } from '@/lib/api/generated/models';

const FORMAT_LABELS: Record<string, string> = {
  ROUND_ROBIN: 'Round Robin',
  ELIMINATION: 'Eliminatoria',
  GROUPS_AND_ELIMINATION: 'Grupos + Eliminatoria',
};

export default function TournamentCard({ tournament }: { tournament: TournamentResponse }) {
  return (
    <Card className="flex flex-col border border-zinc-100 hover:border-zinc-300 transition-colors">
      <CardHeader className="pb-2">
        <div className="flex items-start justify-between gap-2">
          <CardTitle className="text-base font-medium">{tournament.name}</CardTitle>
          <StatusBadge status={tournament.status} />
        </div>
        <p className="text-sm text-zinc-400">
          {FORMAT_LABELS[tournament.format] ?? tournament.format}
        </p>
      </CardHeader>
      <CardContent className="flex-1 space-y-2">
        <div className="flex items-center gap-2 text-sm text-zinc-500">
          <Calendar className="h-3.5 w-3.5 text-zinc-400" />
          <span>
            {format(new Date(tournament.startDate), 'd MMM', { locale: es })} —{' '}
            {format(new Date(tournament.endDate), 'd MMM yyyy', { locale: es })}
          </span>
        </div>
        <div className="flex items-center gap-2 text-sm text-zinc-500">
          <Users className="h-3.5 w-3.5 text-zinc-400" />
          <span>Máx. {tournament.maxPairs} parejas</span>
        </div>
        <Link href={`/tournaments/${tournament.tournamentId}`} className="block mt-3">
          <Button
            size="sm"
            className="w-full"
            variant={tournament.status === 'REGISTRATION_OPEN' ? 'default' : 'outline'}
          >
            {tournament.status === 'REGISTRATION_OPEN' ? 'Ver e inscribirse' : 'Ver torneo'}
          </Button>
        </Link>
      </CardContent>
    </Card>
  );
}
