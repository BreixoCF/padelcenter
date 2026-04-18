'use client';
import Link from 'next/link';
import { useAuthStore } from '@/lib/auth/store';
import { useUserBookings } from '@/hooks/useCenters';
import PageHeader from '@/components/shared/PageHeader';
import BookingCard from '@/components/bookings/BookingCard';
import { GridSkeleton } from '@/components/shared/LoadingState';
import EmptyState from '@/components/shared/EmptyState';
import { Button } from '@/components/ui/button';
import { CalendarX } from 'lucide-react';

export default function DashboardPage() {
  const { user } = useAuthStore();
  const { data, isLoading } = useUserBookings(user?.userId ?? '', { page: 0, size: 10 });
  const bookings = data?.content ?? [];

  return (
    <main className="container mx-auto px-4 py-8 max-w-4xl">
      <PageHeader
        title={`Hola, ${user?.firstName ?? 'usuario'}`}
        description="Tus próximas reservas"
        action={
          <Link href="/centers">
            <Button size="sm">Nueva reserva</Button>
          </Link>
        }
      />

      {isLoading ? (
        <GridSkeleton count={3} />
      ) : bookings.length === 0 ? (
        <EmptyState
          icon={CalendarX}
          title="Sin reservas"
          description="Reserva tu primera pista de pádel"
          action={
            <Link href="/centers">
              <Button variant="outline" size="sm">
                Ver centros
              </Button>
            </Link>
          }
        />
      ) : (
        <div className="space-y-3">
          {bookings.map((booking: any) => (
            <BookingCard key={booking.bookingId} booking={booking} />
          ))}
        </div>
      )}
    </main>
  );
}
