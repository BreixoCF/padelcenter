import CenterDetailClient from './CenterDetailClient';

export default function CenterDetailPage({ params }: { params: { centerId: string } }) {
  return <CenterDetailClient centerId={params.centerId} />;
}
