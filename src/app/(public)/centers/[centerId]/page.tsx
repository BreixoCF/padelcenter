import { Metadata } from 'next';
import { MapPin, Phone } from 'lucide-react';
import type { Center } from '@/types';

interface Props {
  params: { centerId: string };
}

async function getCenter(centerId: string): Promise<Center | null> {
  try {
    const res = await fetch(`${process.env.API_URL}/api/v1/centers/${centerId}`, {
      next: { revalidate: 300 },
    });
    if (!res.ok) return null;
    return res.json();
  } catch {
    return null;
  }
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const center = await getCenter(params.centerId);
  return {
    title: center?.name ?? 'Centro',
    description: center ? `Reserva pistas en ${center.name}, ${center.city}` : undefined,
  };
}

export default async function CenterDetailPage({ params }: Props) {
  const center = await getCenter(params.centerId);

  if (!center) {
    return (
      <main className="container mx-auto px-4 py-8">
        <p className="text-muted-foreground">Centro no encontrado.</p>
      </main>
    );
  }

  return (
    <main className="container mx-auto px-4 py-8 space-y-4">
      <h1 className="text-3xl font-semibold">{center.name}</h1>
      <div className="flex items-center gap-2 text-muted-foreground">
        <MapPin className="h-4 w-4" />
        <span>{center.address}, {center.city}</span>
      </div>
      {center.phone && (
        <div className="flex items-center gap-2 text-muted-foreground">
          <Phone className="h-4 w-4" />
          <span>{center.phone}</span>
        </div>
      )}
    </main>
  );
}
