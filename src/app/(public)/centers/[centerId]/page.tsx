import { Metadata } from 'next';
import CenterDetailClient from './CenterDetailClient';

interface Props {
  params: { centerId: string };
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  try {
    const res = await fetch(
      `${process.env.API_URL}/api/v1/centers/${params.centerId}`,
      { next: { revalidate: 300 } }
    );
    const center = await res.json();
    return {
      title: center.name,
      description: `Reserva pistas en ${center.name} — ${center.city}`,
    };
  } catch {
    return { title: 'Centro de pádel' };
  }
}

export default function CenterDetailPage({ params }: Props) {
  return <CenterDetailClient centerId={params.centerId} />;
}
