import { Metadata } from 'next';
import CenterList from '@/components/centers/CenterList';
import PageHeader from '@/components/shared/PageHeader';

export const metadata: Metadata = {
  title: 'Centros de pádel',
  description: 'Encuentra y reserva pistas en los mejores centros de pádel cerca de ti',
};

async function getCenters() {
  try {
    const res = await fetch(`${process.env.API_URL}/api/v1/centers?page=0&size=20`, {
      next: { revalidate: 300 },
    });
    if (!res.ok) return { content: [] };
    return res.json();
  } catch {
    return { content: [] };
  }
}

export default async function CentersPage() {
  const initialData = await getCenters();
  return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <PageHeader
        title="Centros de pádel"
        description="Encuentra el centro más cercano y reserva tu pista"
      />
      <CenterList initialData={initialData} />
    </main>
  );
}
