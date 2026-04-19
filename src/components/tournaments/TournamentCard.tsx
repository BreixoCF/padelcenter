'use client';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Calendar, Users } from 'lucide-react';
import Link from 'next/link';

const FORMAT_LABELS: Record<string, string> = {
  ROUND_ROBIN: 'Round Robin',
  ELIMINATION: 'Eliminatoria',
  GROUPS_AND_ELIMINATION: 'Grupos + Eliminatoria',
};

const STATUS_VARIANTS: Record<string, any> = {
  DRAFT: 'secondary',
  REGISTRATION_OPEN: 'default',
  REGISTRATION_CLOSED: 'outline',
  IN_PROGRESS: 'default',
  COMPLETED: 'secondary',
  CANCELLED: 'destructive',
};

const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Borrador',
  REGISTRATION_OPEN: 'Inscripción abierta',
  REGISTRATION_CLOSED: 'Inscripción cerrada',
  IN_PROGRESS: 'En curso',
  COMPLETED: 'Finalizado',
  CANCELLED: 'Cancelado',
};

export default function TournamentCard({ tournament }: { tournament: any }) {
  return (
    <Card className="flex flex-col">
      <CardHeader className="pb-2">
        <div className="flex items-start justify-between gap-2">
          <CardTitle className="text-base font-medium">{tournament.name}</CardTitle>
          <Badge variant={STATUS_VARIANTS[tournament.status] ?? 'outline'}>
            {STATUS_LABELS[tournament.status] ?? tournament.status}
          </Badge>
        </div>
        <p className="text-sm text-slate-500">
          {FORMAT_LABELS[tournament.format] ?? tournament.format}
        </p>
      </CardHeader>
      <CardContent className="flex-1 space-y-2">
        <div className="flex items-center gap-2 text-sm text-slate-600">
          <Calendar className="h-4 w-4" />
          <span>
            {format(new Date(tournament.startDate), 'd MMM', { locale: es })} —{' '}
            {format(new Date(tournament.endDate), 'd MMM yyyy', { locale: es })}
          </span>
        </div>
        <div className="flex items-center gap-2 text-sm text-slate-600">
          <Users className="h-4 w-4" />
          <span>Máx. {tournament.maxPairs} parejas</span>
        </div>
        {tournament.status === 'REGISTRATION_OPEN' && (
          <Link href={`/tournaments/${tournament.tournamentId}`} className="block mt-3">
            <Button size="sm" className="w-full">
              Ver e inscribirse
            </Button>
          </Link>
        )}
      </CardContent>
    </Card>
  );
}
