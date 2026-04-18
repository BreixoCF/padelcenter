'use client';
import CenterCard from './CenterCard';
import { Skeleton } from '@/components/ui/skeleton';
import type { Center, PageResponse } from '@/types';

interface CenterListProps {
  initialData: PageResponse<Center>;
}

export default function CenterList({ initialData }: CenterListProps) {
  const centers = initialData.content ?? [];

  if (!centers.length) {
    return <p className="text-muted-foreground">No hay centros disponibles.</p>;
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      {centers.map((center) => (
        <CenterCard key={center.centerId} center={center} />
      ))}
    </div>
  );
}

export function CenterListSkeleton() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      {Array.from({ length: 6 }).map((_, i) => (
        <Skeleton key={i} className="h-40 rounded-lg" />
      ))}
    </div>
  );
}
