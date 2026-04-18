import { Metadata } from 'next';
import CenterList from '@/components/centers/CenterList';
import type { PageResponse, Center } from '@/types';

export const metadata: Metadata = {
  title: 'Centros de pádel',
  description: 'Encuentra y reserva pistas en los mejores centros de pádel',
};

async function getCenters(): Promise<PageResponse<Center>> {
  try {
    const res = await fetch(`${process.env.API_URL}/api/v1/centers?size=20`, {
      next: { revalidate: 300 },
    });
    if (!res.ok) return { content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 };
    return res.json();
  } catch {
    return { content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 };
  }
}

export default async function CentersPage() {
  const data = await getCenters();
  return (
    <main className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-semibold mb-6">Centros de pádel</h1>
      <CenterList initialData={data} />
    </main>
  );
}
