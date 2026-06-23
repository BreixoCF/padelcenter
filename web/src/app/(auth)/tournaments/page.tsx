'use client';
import { useState } from 'react';
import { useListCenters, useGetTournamentsByCenter } from '@/lib/api/generated/centers/centers';
import PageHeader from '@/components/shared/PageHeader';
import TournamentCard from '@/components/tournaments/TournamentCard';
import { GridSkeleton } from '@/components/shared/LoadingState';
import EmptyState from '@/components/shared/EmptyState';
import { Trophy } from 'lucide-react';
import {
  Select, SelectContent, SelectItem,
  SelectTrigger, SelectValue,
} from '@/components/ui/select';

export default function TournamentsPage() {
  const [selectedCenter, setSelectedCenter] = useState<string>('');
  const { data: centersData } = useListCenters({ page: 0, size: 50 });
  const { data: tournamentsData, isLoading } = useGetTournamentsByCenter(
    selectedCenter,
    { page: 0, size: 20 },
    { query: { enabled: !!selectedCenter } }
  );

  const centers = centersData?.content ?? [];
  const tournaments = tournamentsData?.content ?? [];

  return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <PageHeader
        title="Torneos"
        description="Inscríbete en torneos de pádel por parejas"
      />

      <div className="mb-6 max-w-xs">
        <Select onValueChange={setSelectedCenter}>
          <SelectTrigger>
            <SelectValue placeholder="Selecciona un centro" />
          </SelectTrigger>
          <SelectContent>
            {centers.map((c: any) => (
              <SelectItem key={c.centerId} value={c.centerId}>
                {c.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {!selectedCenter ? (
        <EmptyState
          icon={Trophy}
          title="Selecciona un centro"
          description="Elige un centro para ver sus torneos"
        />
      ) : isLoading ? (
        <GridSkeleton count={3} />
      ) : tournaments.length === 0 ? (
        <EmptyState
          icon={Trophy}
          title="No hay torneos disponibles"
          description="Este centro no tiene torneos activos"
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {tournaments.map((t: any) => (
            <TournamentCard key={t.tournamentId} tournament={t} />
          ))}
        </div>
      )}
    </main>
  );
}
