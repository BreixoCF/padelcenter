import { Metadata } from 'next';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import type { PageResponse, Tournament } from '@/types';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export const metadata: Metadata = {
  title: 'Torneos',
  description: 'Participa en torneos de pádel cerca de ti',
};

async function getTournaments(): Promise<PageResponse<Tournament>> {
  try {
    const res = await fetch(`${process.env.API_URL}/api/v1/tournaments?size=20`, {
      next: { revalidate: 300 },
    });
    if (!res.ok) return { content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 };
    return res.json();
  } catch {
    return { content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 };
  }
}

const statusLabel: Record<Tournament['status'], string> = {
  OPEN: 'Abierto',
  IN_PROGRESS: 'En curso',
  FINISHED: 'Finalizado',
};

const statusVariant: Record<Tournament['status'], 'default' | 'secondary' | 'outline'> = {
  OPEN: 'default',
  IN_PROGRESS: 'secondary',
  FINISHED: 'outline',
};

export default async function TournamentsPage() {
  const data = await getTournaments();

  return (
    <main className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-semibold mb-6">Torneos</h1>
      {data.content.length === 0 ? (
        <p className="text-muted-foreground">No hay torneos disponibles.</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {data.content.map((t) => (
            <Card key={t.tournamentId}>
              <CardHeader>
                <div className="flex items-start justify-between gap-2">
                  <CardTitle className="text-lg">{t.name}</CardTitle>
                  <Badge variant={statusVariant[t.status]}>{statusLabel[t.status]}</Badge>
                </div>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground space-y-1">
                <p>Inicio: {format(new Date(t.startDate), 'PPP', { locale: es })}</p>
                <p>Fin: {format(new Date(t.endDate), 'PPP', { locale: es })}</p>
                <p>Plazas: {t.maxPlayers}</p>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </main>
  );
}
