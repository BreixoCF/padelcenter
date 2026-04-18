'use client';
import { useState } from 'react';
import { useCenter, useCenterFields } from '@/hooks/useCenters';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { MapPin, Phone } from 'lucide-react';
import FieldCard from '@/components/fields/FieldCard';
import BookingModal from '@/components/bookings/BookingModal';
import { GridSkeleton } from '@/components/shared/LoadingState';
import ErrorState from '@/components/shared/ErrorState';

export default function CenterDetailClient({ centerId }: { centerId: string }) {
  const [selectedField, setSelectedField] = useState<{
    fieldId: string;
    pricePerHour: number;
  } | null>(null);
  const [typeFilter, setTypeFilter] = useState<string | undefined>();

  const { data: center, isLoading: centerLoading } = useCenter(centerId);
  const {
    data: fieldsData,
    isLoading: fieldsLoading,
    isError,
    refetch,
  } = useCenterFields(centerId, { type: typeFilter });

  const fields = fieldsData?.content ?? [];

  if (centerLoading)
    return (
      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <GridSkeleton count={3} />
      </div>
    );

  const FieldGrid = () =>
    fieldsLoading ? (
      <GridSkeleton count={4} />
    ) : isError ? (
      <ErrorState onRetry={() => refetch()} />
    ) : (
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {fields.map((field: any) => (
          <FieldCard
            key={field.fieldId}
            field={field}
            onBook={(fieldId, pricePerHour) =>
              setSelectedField({ fieldId, pricePerHour })
            }
          />
        ))}
      </div>
    );

  return (
    <main className="container mx-auto px-4 py-8 max-w-4xl">
      <div className="mb-6">
        <div className="flex items-start justify-between">
          <h1 className="text-2xl font-semibold">{center?.name}</h1>
          <Badge variant="secondary">{center?.city}</Badge>
        </div>
        <div className="mt-2 space-y-1 text-sm text-slate-600">
          <div className="flex items-center gap-2">
            <MapPin className="h-4 w-4" />
            <span>{center?.address}</span>
          </div>
          {center?.phoneNumber && (
            <div className="flex items-center gap-2">
              <Phone className="h-4 w-4" />
              <span>{center.phoneNumber}</span>
            </div>
          )}
        </div>
      </div>

      <Tabs
        defaultValue="all"
        onValueChange={(v) => setTypeFilter(v === 'all' ? undefined : v)}
      >
        <TabsList className="mb-4">
          <TabsTrigger value="all">Todas</TabsTrigger>
          <TabsTrigger value="INDOOR">Interior</TabsTrigger>
          <TabsTrigger value="OUTDOOR">Exterior</TabsTrigger>
        </TabsList>
        <TabsContent value="all">
          <FieldGrid />
        </TabsContent>
        <TabsContent value="INDOOR">
          <FieldGrid />
        </TabsContent>
        <TabsContent value="OUTDOOR">
          <FieldGrid />
        </TabsContent>
      </Tabs>

      {selectedField && (
        <BookingModal
          fieldId={selectedField.fieldId}
          pricePerHour={selectedField.pricePerHour}
          onClose={() => setSelectedField(null)}
        />
      )}
    </main>
  );
}
