'use client';
import { useListCenters } from '@/lib/api/generated/centers/centers';
import CenterCard from '@/components/centers/CenterCard';
import PageHeader from '@/components/shared/PageHeader';
import { GridSkeleton } from '@/components/shared/LoadingState';
import ErrorState from '@/components/shared/ErrorState';
import EmptyState from '@/components/shared/EmptyState';
import { Building2 } from 'lucide-react';

export default function CentersPage() {
  const { data, isLoading, isError, refetch } = useListCenters({ page: 0, size: 20 });
  const centers = data?.content ?? [];

  if (isLoading)
    return (
      <main className="container mx-auto px-4 py-8 max-w-6xl">
        <PageHeader title="Centros de pádel" />
        <GridSkeleton />
      </main>
    );

  if (isError)
    return (
      <main className="container mx-auto px-4 py-8 max-w-6xl">
        <PageHeader title="Centros de pádel" />
        <ErrorState onRetry={() => refetch()} />
      </main>
    );

  return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <PageHeader
        title="Centros de pádel"
        description="Selecciona un centro para ver sus pistas"
      />
      {centers.length === 0 ? (
        <EmptyState
          icon={Building2}
          title="No hay centros disponibles"
          description="Vuelve a intentarlo más tarde"
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {centers.map((center) => (
            <CenterCard key={center.centerId} center={center} />
          ))}
        </div>
      )}
    </main>
  );
}
