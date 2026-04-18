'use client';
import { useListCenters, useGetTournamentsByCenter } from '@/lib/api/generated/centers/centers';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import PageHeader from '@/components/shared/PageHeader';
import { GridSkeleton } from '@/components/shared/LoadingState';
import EmptyState from '@/components/shared/EmptyState';
import { Trophy } from 'lucide-react';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { TournamentResponseStatus } from '@/lib/api/generated/models/tournamentResponseStatus';

const STATUS_LABEL: Partial<Record<TournamentResponseStatus, string>> = {
  [TournamentResponseStatus.DRAFT]: 'Borrador',
  [TournamentResponseStatus.REGISTRATION_OPEN]: 'Inscripción abierta',
  [TournamentResponseStatus.REGISTRATION_CLOSED]: 'Inscripción cerrada',
  [TournamentResponseStatus.IN_PROGRESS]: 'En curso',
  [TournamentResponseStatus.COMPLETED]: 'Completado',
  [TournamentResponseStatus.CANCELLED]: 'Cancelado',
};

const STATUS_VARIANT: Partial<
  Record<TournamentResponseStatus, 'default' | 'secondary' | 'outline' | 'destructive'>
> = {
  [TournamentResponseStatus.REGISTRATION_OPEN]: 'default',
  [TournamentResponseStatus.IN_PROGRESS]: 'secondary',
  [TournamentResponseStatus.COMPLETED]: 'outline',
  [TournamentResponseStatus.CANCELLED]: 'destructive',
};

function CenterTournaments({ centerId }: { centerId: string }) {
  const { data } = useGetTournamentsByCenter(centerId, { size: 10 });
  const items = data?.content ?? [];
  if (!items.length) return null;

  return (
    <>
      {items.map((t) => (
        <Card key={t.tournamentId}>
          <CardHeader className="pb-2">
            <div className="flex items-start justify-between gap-2">
              <CardTitle className="text-base font-medium">{t.name}</CardTitle>
              <Badge variant={STATUS_VARIANT[t.status] ?? 'outline'}>
                {STATUS_LABEL[t.status] ?? t.status}
              </Badge>
            </div>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground space-y-1">
            {t.description && <p className="text-slate-700">{t.description}</p>}
            <p>Inicio: {format(new Date(t.startDate), 'PPP', { locale: es })}</p>
            <p>Fin: {format(new Date(t.endDate), 'PPP', { locale: es })}</p>
            <p>Parejas máx.: {t.maxPairs}</p>
          </CardContent>
        </Card>
      ))}
    </>
  );
}

export default function TournamentsPage() {
  const { data: centersData, isLoading } = useListCenters({ page: 0, size: 50 });
  const centers = centersData?.content ?? [];

  if (isLoading)
    return (
      <main className="container mx-auto px-4 py-8 max-w-6xl">
        <PageHeader title="Torneos" />
        <GridSkeleton />
      </main>
    );

  if (!centers.length)
    return (
      <main className="container mx-auto px-4 py-8 max-w-6xl">
        <PageHeader title="Torneos" />
        <EmptyState
          icon={Trophy}
          title="No hay torneos disponibles"
          description="Vuelve a intentarlo más tarde"
        />
      </main>
    );

  return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <PageHeader
        title="Torneos"
        description="Participa en torneos de pádel en los centros de la plataforma"
      />
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {centers.map((c) => (
          <CenterTournaments key={c.centerId} centerId={c.centerId} />
        ))}
      </div>
    </main>
  );
}
