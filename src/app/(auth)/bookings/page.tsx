'use client';
import { useState } from 'react';
import { useGetMyBookings } from '@/lib/api/generated/auth/auth';
import { useCancelBooking } from '@/lib/api/generated/bookings/bookings';
import PageHeader from '@/components/shared/PageHeader';
import BookingCard from '@/components/bookings/BookingCard';
import { GridSkeleton } from '@/components/shared/LoadingState';
import EmptyState from '@/components/shared/EmptyState';
import { Button } from '@/components/ui/button';
import {
  Select, SelectContent, SelectItem,
  SelectTrigger, SelectValue,
} from '@/components/ui/select';
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel,
  AlertDialogContent, AlertDialogDescription,
  AlertDialogFooter, AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { useToast } from '@/components/ui/use-toast';
import { useQueryClient } from '@tanstack/react-query';
import { CalendarX } from 'lucide-react';
import BookingModal from '@/components/bookings/BookingModal';

const STATUS_OPTIONS = [
  { value: 'ALL', label: 'Todas' },
  { value: 'CONFIRMED', label: 'Confirmadas' },
  { value: 'CANCELLED', label: 'Canceladas' },
  { value: 'COMPLETED', label: 'Completadas' },
];

export default function BookingsPage() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState('ALL');
  const [cancelId, setCancelId] = useState<number | null>(null);
  const [editBooking, setEditBooking] = useState<{
    bookingId: number;
    fieldId: string;
    pricePerHour: number;
  } | null>(null);

  const { data, isLoading } = useGetMyBookings({ page, size: 10 });

  const { mutate: cancelBooking, isPending } = useCancelBooking();

  const bookings = (data?.content ?? []).filter((b: any) =>
    status === 'ALL' ? true : b.status === status
  );

  const handleCancel = () => {
    if (!cancelId) return;
    cancelBooking(
      { bookingId: cancelId },
      {
        onSuccess: () => {
          toast({ title: 'Reserva cancelada' });
          queryClient.invalidateQueries({ queryKey: ['/api/v1/me/bookings'] });
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
        title="Historial de reservas"
        description={`${data?.totalElements ?? 0} reservas en total`}
      />

      <div className="mb-4 max-w-xs">
        <Select value={status} onValueChange={(v) => { setStatus(v); setPage(0); }}>
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {STATUS_OPTIONS.map(o => (
              <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <GridSkeleton count={4} />
      ) : bookings.length === 0 ? (
        <EmptyState
          icon={CalendarX}
          title="Sin reservas"
          description="No tienes reservas con este filtro"
        />
      ) : (
        <>
          <div className="space-y-3">
            {bookings.map((booking: any) => (
              <BookingCard
                key={booking.bookingId}
                booking={booking}
                onEdit={booking.status === 'CONFIRMED'
                  ? () => setEditBooking({
                      bookingId: booking.bookingId,
                      fieldId: booking.field?.fieldId ?? '',
                      pricePerHour: booking.totalPrice,
                    })
                  : undefined}
                onCancel={booking.status === 'CONFIRMED'
                  ? () => setCancelId(booking.bookingId)
                  : undefined}
              />
            ))}
          </div>

          {data && data.totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-4">
              <Button variant="outline" size="sm"
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}>
                Anterior
              </Button>
              <span className="text-sm text-slate-500 flex items-center px-2">
                {page + 1} / {data.totalPages}
              </span>
              <Button variant="outline" size="sm"
                disabled={page >= data.totalPages - 1}
                onClick={() => setPage(p => p + 1)}>
                Siguiente
              </Button>
            </div>
          )}
        </>
      )}

      {editBooking && (
        <BookingModal
          fieldId={editBooking.fieldId}
          pricePerHour={editBooking.pricePerHour}
          existingBooking={{ bookingId: editBooking.bookingId }}
          onClose={() => {
            setEditBooking(null);
            queryClient.invalidateQueries({ queryKey: ['/api/v1/me/bookings'] });
          }}
        />
      )}

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
              disabled={isPending}
              className="bg-red-600 hover:bg-red-700">
              {isPending ? 'Cancelando...' : 'Cancelar reserva'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </main>
  );
}
