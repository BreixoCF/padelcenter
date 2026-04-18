'use client';
import { useCenters } from '@/hooks/useCenters';
import CenterCard from './CenterCard';
import { GridSkeleton } from '@/components/shared/LoadingState';
import ErrorState from '@/components/shared/ErrorState';
import EmptyState from '@/components/shared/EmptyState';
import { Building2 } from 'lucide-react';

interface CenterListProps {
  initialData?: { content: any[] };
}

export default function CenterList({ initialData }: CenterListProps) {
  const { data, isLoading, isError, refetch } = useCenters(0, 20);
  const centers = data?.content ?? initialData?.content ?? [];

  if (isLoading && !initialData) return <GridSkeleton />;
  if (isError) return <ErrorState onRetry={() => refetch()} />;
  if (!centers.length)
    return (
      <EmptyState
        icon={Building2}
        title="No hay centros disponibles"
        description="Vuelve a intentarlo más tarde"
      />
    );

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {centers.map((center: any) => (
        <CenterCard key={center.centerId} center={center} />
      ))}
    </div>
  );
}
