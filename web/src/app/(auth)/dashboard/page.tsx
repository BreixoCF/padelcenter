'use client';
import { useState } from 'react';
import { useAuthStore } from '@/lib/auth/store';
import { useGetUserBookings, useCancelBooking } from '@/lib/api/generated/bookings/bookings';
import { useGetMyTournaments } from '@/lib/api/generated/auth/auth';
import PageHeader from '@/components/shared/PageHeader';
import BookingCard from '@/components/bookings/BookingCard';
import TournamentCard from '@/components/tournaments/TournamentCard';
import { GridSkeleton } from '@/components/shared/LoadingState';
import EmptyState from '@/components/shared/EmptyState';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { CalendarX, Trophy } from 'lucide-react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel,
  AlertDialogContent, AlertDialogDescription,
  AlertDialogFooter, AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { useToast } from '@/components/ui/use-toast';
import { useQueryClient } from '@tanstack/react-query';

export default function DashboardPage() {
  const { user } = useAuthStore();
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [cancelId, setCancelId] = useState<number | null>(null);

  const { data: bookingsData, isLoading: bookingsLoading } = useGetUserBookings(
    user?.userId ?? '',
    { page: 0, size: 5 },
    { query: { enabled: !!user?.userId } }
  );

  const { data: tournamentsData, isLoading: tournamentsLoading } = useGetMyTournaments(
    { page: 0, size: 3 },
    { query: { enabled: !!user?.userId } }
  );

  const { mutate: cancelBooking, isPending: isCancelling } = useCancelBooking();

  const upcomingBookings = (bookingsData?.content ?? []).filter((b: any) => b.status === 'CONFIRMED');
  const myTournaments = tournamentsData?.content ?? [];

  const handleCancel = () => {
    if (!cancelId) return;
    cancelBooking(
      { bookingId: cancelId },
      {
        onSuccess: () => {
          toast({ title: 'Reserva cancelada' });
          queryClient.invalidateQueries({ queryKey: [`/api/v1/users/${user?.userId}/bookings`] });
          setCancelId(null);
        },
        onError: (err: any) => {
          toast({
            title: 'Error al cancelar',
            description: err.response?.data?.detail,
            variant: 'destructive',
          });
          setCancelId(null);
        },
      }
    );
  };

  return (
    <main className="container mx-auto px-4 py-8 max-w-4xl">
      <PageHeader
        title={`Hola, ${user?.firstName}`}
        description="Tu actividad en PadelCenter"
      />

      <Tabs defaultValue="bookings">
        <TabsList className="mb-4">
          <TabsTrigger value="bookings">Mis reservas</TabsTrigger>
          <TabsTrigger value="tournaments">Mis torneos</TabsTrigger>
        </TabsList>

        <TabsContent value="bookings">
          {bookingsLoading ? (
            <GridSkeleton count={3} />
          ) : upcomingBookings.length === 0 ? (
            <EmptyState
              icon={CalendarX}
              title="Sin reservas activas"
              description="Reserva una pista para empezar"
              action={
                <Link href="/centers">
                  <Button variant="outline" size="sm">Ver centros</Button>
                </Link>
              }
            />
          ) : (
            <div className="space-y-3">
              {upcomingBookings.map((booking: any) => (
                <BookingCard
                  key={booking.bookingId}
                  booking={booking}
                  onCancel={booking.status === 'CONFIRMED'
                    ? () => setCancelId(booking.bookingId)
                    : undefined}
                />
              ))}
              <Link href="/bookings" className="block text-center">
                <Button variant="ghost" size="sm" className="text-slate-500">
                  Ver historial completo →
                </Button>
              </Link>
            </div>
          )}
        </TabsContent>

        <TabsContent value="tournaments">
          {tournamentsLoading ? (
            <GridSkeleton count={2} />
          ) : myTournaments.length === 0 ? (
            <EmptyState
              icon={Trophy}
              title="Sin torneos"
              description="Inscríbete en un torneo"
              action={
                <Link href="/tournaments">
                  <Button variant="outline" size="sm">Ver torneos</Button>
                </Link>
              }
            />
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {myTournaments.map((t: any) => (
                <TournamentCard key={t.tournamentId} tournament={t} />
              ))}
              <Link href="/tournaments" className="col-span-full text-center block">
                <Button variant="ghost" size="sm" className="text-slate-500">
                  Ver todos los torneos →
                </Button>
              </Link>
            </div>
          )}
        </TabsContent>
      </Tabs>

      <AlertDialog open={!!cancelId} onOpenChange={() => setCancelId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Cancelar esta reserva?</AlertDialogTitle>
            <AlertDialogDescription>Esta acción no se puede deshacer.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Volver</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleCancel}
              disabled={isCancelling}
              className="bg-red-600 hover:bg-red-700">
              {isCancelling ? 'Cancelando...' : 'Cancelar reserva'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </main>
  );
}
